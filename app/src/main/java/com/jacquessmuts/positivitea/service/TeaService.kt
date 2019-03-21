package com.jacquessmuts.positivitea.service

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jacquessmuts.positivitea.database.TeaDb
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
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the db and exposes db items to the app
 */
class TeaService(private val db: TeaDb) : CoroutineService {

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    var timeState: TimeState? = null
        set(value) {
            if (value != field) {
                field = value
                saveTimeState()
            }
        }

    private val timeStateDbObserver by lazy {
        TimeStateDbObserver(
            db.timeStateDao(),
            timeStateDbPublisher
        )
    }
    private val timeStateDbPublisher: PublishSubject<TimeState> by lazy {
        PublishSubject.create<TimeState>()
    }

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
            db.teabagDao(),
            teaBagDbPublisher
        )
    }
    private val teaBagDbPublisher: PublishSubject<List<TeaBag>> by lazy {
        PublishSubject.create<List<TeaBag>>()
    }

    init {
        Timber.i("initializing TeaService")

        launch {
            loadTimeState()
            getTeabagsFromDb()
            getTeabagsFromServer()
        }

        db.invalidationTracker.addObserver(teabagDbObserver)
        db.invalidationTracker.addObserver(timeStateDbObserver)

        rxSubs.add(teaBagDbPublisher.subscribeAndLogE {
            allTeaBags = it
        })

        rxSubs.add(timeStateDbPublisher.subscribeAndLogE {
            timeState = it
        })

        // If nothing is listening to this service for 10 seconds, twice in a row, go to sleep
        rxSubs.add(Observable.interval(10, TimeUnit.SECONDS)
            .map { teabagPublisher.hasObservers() }
            .buffer(2)
            .filter {
                var isObserved = true
                it.forEach {
                    isObserved = isObserved && it
                }
                !isObserved
            }
            .subscribeAndLogE {
                clear()
            })
    }

    private suspend fun getTeabagsFromDb() {
        suspendCoroutine<Boolean> { continuation ->
            allTeaBags = db.teabagDao().allTeaBags
            continuation.resume(allTeaBags.isEmpty())
        }
    }

    fun saveTimeState() {
        timeState?.let {
            launch {
                db.timeStateDao().insert(it)
            }
        }
    }

    private suspend fun loadTimeState() {
        suspendCoroutine<Boolean> { continuation ->
            val nuTimeState: TimeState? = db.timeStateDao().timeState
            timeState = nuTimeState ?: TimeState(timeTeabagsUpdated = 0)
            continuation.resume(true)
        }
    }

    fun saveTeabags(teaBags: List<TeaBag>) {
        launch {
            teaBags.forEach {
                db.teabagDao().insert(it)
            }
        }
    }

    private fun clear() {
        clearJobs()
        rxSubs.dispose()
        db.invalidationTracker.removeObserver(teabagDbObserver)
        db.invalidationTracker.removeObserver(timeStateDbObserver)
    }

    /**
     *
     */
    suspend fun getTeabagsFromServer(forceLoad: Boolean = false) {

        Timber.v("Considering getting teabags from server")

        // There might be a race condition here where timeState could be null on first startup, hence == true
        if (!(timeState?.canMakeNewApiCall == true || forceLoad))
            return

        Timber.d("Getting teabags from server")

        val firestore = FirebaseFirestore.getInstance()

        suspendCoroutine<Boolean> { continuation ->
            firestore.collection(FirestoreConstants.COLLECTION_TEABAGS)
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            val nuTeaBags = mutableListOf<TeaBag>()
                            for (document in task.getResult()!!) {
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
                })
        }
    }
}