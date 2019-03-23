package com.jacquessmuts.positivitea.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jacquessmuts.positivitea.database.TeaDatabase
import com.jacquessmuts.positivitea.database.TeabagDbObserver
import com.jacquessmuts.positivitea.database.TimeStateDbObserver
import com.jacquessmuts.positivitea.firestore.FirestoreConstants
import com.jacquessmuts.positivitea.model.TeaBag
import com.jacquessmuts.positivitea.model.TimeState
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the teaDb and exposes teaDb items to the app
 */
class TeaRepository(private val teaDb: TeaDatabase) : CoroutineService {

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    private var timeState: TimeState? = null
        set(value) {
            if (value != field) {
                field = value
                saveTimeState()
            }
        }

    private val timeStateDbObserver by lazy {
        TimeStateDbObserver(
            teaDb.timeStateDao(),
            timeStateDbPublisher
        )
    }
    private val timeStateDbPublisher: PublishSubject<TimeState> by lazy {
        PublishSubject.create<TimeState>()
    }

    val liveTeaBags = teaDb.teabagDao().liveAllTeaBags
    val liveTeaPreferences = teaDb.teaPreferencesDao().liveTeaPreferences

    var allTeaBags: List<TeaBag> = listOf()
        private set(nuTeabags) {
            field = nuTeabags
            teabagPublisher.onNext(Any())
        }

    private val teabagPublisher: PublishSubject<Any> by lazy {
        PublishSubject.create<Any>()
    }
    val teabagObservable: Observable<Any>
        get() = teabagPublisher.hide()

    private val teabagDbObserver by lazy {
        TeabagDbObserver(
            teaDb.teabagDao(),
            teaBagDbPublisher
        )
    }
    private val teaBagDbPublisher: PublishSubject<List<TeaBag>> by lazy {
        PublishSubject.create<List<TeaBag>>()
    }

    init {
        Timber.i("initializing TeaRepository")

        launch {
            loadTimeState()
            getTeabagsFromDb()
            getTeabagsFromServer()
        }

        teaDb.invalidationTracker.addObserver(teabagDbObserver)
        teaDb.invalidationTracker.addObserver(timeStateDbObserver)

        rxSubs.add(teaBagDbPublisher.subscribeAndLogE {
            allTeaBags = it
        })

        rxSubs.add(timeStateDbPublisher.subscribeAndLogE {
            timeState = it
        })

        // If nothing is listening to this service for 10 seconds, twice in a row, go to sleep
        rxSubs.add(Observable.interval(10, TimeUnit.SECONDS)
            .map { liveTeaBags.hasObservers() }
            .buffer(2)
            .filter {
                var isObserved = true
                it.forEach { hasObservers ->
                    isObserved = isObserved && hasObservers
                }
                !isObserved
            }
            .subscribeAndLogE {
                clear()
            })
    }

    private suspend fun getTeabagsFromDb() {
        suspendCoroutine<Boolean> { continuation ->
            allTeaBags = teaDb.teabagDao().allTeaBags
            continuation.resume(allTeaBags.isEmpty())
        }
    }

    private fun saveTimeState() {
        timeState?.let {
            launch {
                teaDb.timeStateDao().insert(it)
            }
        }
    }

    private suspend fun loadTimeState() {
        suspendCoroutine<Boolean> { continuation ->
            val nuTimeState: TimeState? = teaDb.timeStateDao().timeState
            timeState = nuTimeState ?: TimeState(timeTeabagsUpdated = 0)
            continuation.resume(true)
        }
    }

    private fun saveTeabags(teaBags: List<TeaBag>) {
        launch {
            teaBags.forEach {
                teaDb.teabagDao().insert(it)
            }
        }
    }

    private fun clear() {
        clearJobs()
        rxSubs.dispose()
        teaDb.invalidationTracker.removeObserver(teabagDbObserver)
        teaDb.invalidationTracker.removeObserver(timeStateDbObserver)
    }

    /**
     *
     */
    private suspend fun getTeabagsFromServer(forceLoad: Boolean = false) {

        Timber.v("Considering getting teabags from server")

        // There might be a race condition here where timeState could be null on first startup, hence == true
        if (!(timeState?.canMakeNewApiCall == true || forceLoad))
            return

        Timber.d("Getting teabags from server")

        val firestore = FirebaseFirestore.getInstance()

        suspendCoroutine<Boolean> { continuation ->
            firestore.collection(FirestoreConstants.COLLECTION_TEABAGS)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {

                        val nuTeaBags = mutableListOf<TeaBag>()
                        for (document in task.result!!) {
                            nuTeaBags.add(TeaBag(
                                id = document.id,
                                title = document.getString(FirestoreConstants.FIELD_TITLE) ?: "",
                                message = document.getString(FirestoreConstants.FIELD_MESSAGE) ?: "",
                                score = document.getLong(FirestoreConstants.FIELD_SCORE) ?: 0))
                        }

                        saveTeabags(nuTeaBags)
                        timeState = TimeState(timeTeabagsUpdated = System.currentTimeMillis())
                        Timber.i("A total of ${nuTeaBags.size} Teabags downloaded")
                        continuation.resume(true)
                    } else {
                        Timber.e("Error getting documents. ${task.exception}")
                        continuation.resume(false)
                    }
                }
        }
    }

    fun saveTeabagToServer(title: String, message: String, finished: (success: Boolean) -> Unit) {
        require(title.isNotBlank())
        require(message.isNotBlank())

        val nuTeabag = TeaBag(id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            score = 0L)

        val docData = HashMap<String, Any?>()
        docData["id"] = nuTeabag.id
        docData["title"] = nuTeabag.title
        docData["message"] = nuTeabag.message
        docData["score"] = nuTeabag.score

        FirebaseFirestore.getInstance()
            .collection(FirestoreConstants.COLLECTION_TEABAGS)
            .document(nuTeabag.id)
            .set(docData, SetOptions.merge())
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    launch {
                        teaDb.teabagDao().insert(nuTeabag)
                    }
                }
                finished(result.isSuccessful)
            }

    }
}