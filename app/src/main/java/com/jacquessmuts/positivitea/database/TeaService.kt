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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the db and exposes db items to the app
 */
class TeaService(private val db: TeaDb): CoroutineService {

    val timeState by lazy { TimeState() } // TODO: Get from local persistance or whatever

    private val teabagDao: TeabagDao
        get() = db.teabagDao()

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

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }
    private val dbPublisher: PublishSubject<List<Teabag>> by lazy {
        PublishSubject.create<List<Teabag>>()
    }

    fun initialize() {
        Timber.i("initializing TeaService")
        getTeabagsFromServer()
        getTeabagsFromDb()

        db.invalidationTracker.addObserver(TeabagObserver(teabagDao, dbPublisher))

        rxSubs.add(dbPublisher.subscribeAndLogE {
            allTeaBags = it
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
            allTeaBags = teabagDao.allTeabags
        }
    }

    fun insertAll(teabags: List<Teabag>) {
        launch {
            teabags.forEach {
                teabagDao.insert(it)
            }
        }
    }

    fun clear() {
        clearJobs()
        rxSubs.dispose()
    }

    fun getTeabagsFromServer(forceLoad: Boolean = false) {

        Timber.v("Considering getting teabags from server")

        if (!timeState.canMakeNewApiCall && !forceLoad)
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

                        insertAll(nuTeaBags)

                    } else {
                        Timber.e("Error getting documents. ${task.exception}")
                    }
                }

            })
    }

}