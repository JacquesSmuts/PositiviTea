package com.jacquessmuts.positivitea.database

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jacquessmuts.positivitea.CoroutineService
import com.jacquessmuts.positivitea.firestore.FirestoreConstants
import com.jacquessmuts.positivitea.models.Teabag
import com.jacquessmuts.positivitea.models.TimeState
import com.jacquessmuts.positivitea.utils.subscribeAndLogE
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the db and exposes db items to the app
 */
class TeaService(private val db: TeaDb): CoroutineService {

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    var timeState: TimeState? = null

    private val timeStateDbObserver by lazy { TimeStateDbObserver(db.timeStateDao(), timeStateDbPublisher) }
    private val timeStateDbPublisher: PublishSubject<TimeState> by lazy {
        PublishSubject.create<TimeState>()
    }

    var allTeaBags: List<Teabag> = listOf()
        private set(nuTeabags) {
            field = nuTeabags
            teabagPublisher.onNext(Any())
        }

    private val teabagPublisher: PublishSubject<Any> by lazy {
        PublishSubject.create<Any>()
    }
    val teabagObservable: Observable<Any>
        get() = teabagPublisher.hide()

    private val teabagDbObserver by lazy { TeabagDbObserver(db.teabagDao(), teabagDbPublisher) }
    private val teabagDbPublisher: PublishSubject<List<Teabag>> by lazy {
        PublishSubject.create<List<Teabag>>()
    }

    fun initialize() {
        Timber.i("initializing TeaService")

        getTimeStateFromDb()
        getTeabagsFromDb()
        getTeabagsFromServer()

        db.invalidationTracker.addObserver(teabagDbObserver)
        db.invalidationTracker.addObserver(timeStateDbObserver)

        rxSubs.add(teabagDbPublisher.subscribeAndLogE {
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

    private fun getTeabagsFromDb(){
        launch {
            allTeaBags = db.teabagDao().allTeabags
        }
    }

    private fun getTimeStateFromDb(){
        launch {
            timeState = db.timeStateDao().timeState
        }
    }

    fun saveTimeState() {
        timeState?.let {
            launch {
                db.timeStateDao().insert(it)
            }
        }
    }

    fun saveTeabags(teabags: List<Teabag>) {
        launch {
            teabags.forEach {
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

    fun getTeabagsFromServer(forceLoad: Boolean = false) {

        Timber.v("Considering getting teabags from server")

        // There might be a race condition here where timeState could be null on first startup
        if (!(timeState?.canMakeNewApiCall != false || forceLoad))
            return

        Timber.d("Getting teabags from server")

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(FirestoreConstants.COLLECTION_TEABAGS)
            .get()
            .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                override fun onComplete(task: Task<QuerySnapshot>) {
                    if (task.isSuccessful() && task.getResult() != null) {

                        val nuTeaBags = mutableListOf<Teabag>()
                        for (document in task.getResult()!!) {
                            nuTeaBags.add(Teabag(
                                id = document.id,
                                title = document.getString(FirestoreConstants.FIELD_TITLE) ?: "",
                                message = document.getString(FirestoreConstants.FIELD_MESSAGE) ?: "",
                                score = document.getLong(FirestoreConstants.FIELD_SCORE) ?: 0))
                        }

                        saveTeabags(nuTeaBags)
                        timeState = TimeState(timeTeabagsUpdated = System.currentTimeMillis())
                        saveTimeState()
                        Timber.i("A total of ${nuTeaBags.size} Teabags downloaded")

                    } else {
                        Timber.e("Error getting documents. ${task.exception}")
                    }
                }

            })
    }

}