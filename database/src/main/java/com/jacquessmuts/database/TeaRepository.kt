package com.jacquessmuts.database

import androidx.lifecycle.LiveData
import com.jacquessmuts.core.CoroutineService
import com.jacquessmuts.core.subscribeAndLogE
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the teaDb and exposes teaDb items to the app
 *
 * TODO: remove CoroutineService and move relevant functions into a service
 */
class TeaRepository(private val teaDb: TeaDatabase) : CoroutineService {

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    val liveTimeState: LiveData<TimeState> = teaDb.timeStateDao().liveTimeState

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
            getTeabagsFromDb()
        }

        teaDb.invalidationTracker.addObserver(teabagDbObserver)

        rxSubs.add(teaBagDbPublisher.subscribeAndLogE {
            allTeaBags = it
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

    suspend fun loadPreferences(): TeaPreferences {
        val loadedTeaPreferences: TeaPreferences? = teaDb.teaPreferencesDao().teaPreferences()
        return if (loadedTeaPreferences == null) {
            TeaPreferences()
        } else {
            loadedTeaPreferences
        }
    }

    fun savePreferences(teaPreferences: TeaPreferences) {
        GlobalScope.launch(Dispatchers.IO){
            teaDb.teaPreferencesDao().insert(teaPreferences)
        }
    }

    private suspend fun getTeabagsFromDb() {
        suspendCoroutine<Boolean> { continuation ->
            allTeaBags = teaDb.teabagDao().allTeaBags
            continuation.resume(allTeaBags.isEmpty())
        }
    }

    suspend fun getTimeState(): TimeState {
        return teaDb.timeStateDao().timeState()
    }

    fun saveTimeState(timeState: TimeState) {
        GlobalScope.launch(Dispatchers.IO) {
            teaDb.timeStateDao().insert(timeState)
        }
    }

    fun deleteTeabag(id: String) {
        GlobalScope.launch (Dispatchers.IO) {
            teaDb.teabagDao().delete(id)
        }
    }

    fun saveTeabags(teaBags: List<TeaBag>) {
        GlobalScope.launch (Dispatchers.IO) {
            teaBags.forEach {
                teaDb.teabagDao().insert(it)
            }
        }
    }

    private fun clear() {
        clearJobs()
        rxSubs.dispose()
        teaDb.invalidationTracker.removeObserver(teabagDbObserver)
    }

}