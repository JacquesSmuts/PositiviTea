package com.jacquessmuts.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.jacquessmuts.core.CoroutineService
import com.jacquessmuts.core.model.TeaBag
import com.jacquessmuts.core.model.TeaPreferences
import com.jacquessmuts.core.model.TimeState
import com.jacquessmuts.core.subscribeAndLogE
import com.jacquessmuts.database.model.TeaBagTable
import com.jacquessmuts.database.model.TeaPreferencesTable
import com.jacquessmuts.database.model.TimeStateTable
import com.jacquessmuts.database.util.TeabagDbObserver
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
class TeaRepository(private val applicationContext: Context) : CoroutineService {

    private val teaDb: TeaDatabase by lazy { TeaDatabase.getDatabase(applicationContext) }

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    val liveTimeState: LiveData<TimeState> =  Transformations.map(
        teaDb.timeStateDao().liveTimeState(TimeState.ID)) {
        it.toTimeState()
    }

    val liveTeaBags = Transformations.map(teaDb.teabagDao().liveAllTeaBags) {
        it.map { it.toTeaBag() }
    }

    val liveTeaPreferences = Transformations.map(
        teaDb.teaPreferencesDao().liveTeaPreferences(TeaPreferences.ID)) {
        it.toTeaPreferences()
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
            teaDb.teabagDao(),
            teaBagDbPublisher
        )
    }
    private val teaBagDbPublisher: PublishSubject<List<TeaBagTable>> by lazy {
        PublishSubject.create<List<TeaBagTable>>()
    }

    init {
        Timber.i("initializing TeaRepository")

        launch {
            getTeabagsFromDb()
        }

        teaDb.invalidationTracker.addObserver(teabagDbObserver)

        rxSubs.add(teaBagDbPublisher.subscribeAndLogE {
            allTeaBags = it.map { it.toTeaBag() }
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
        val loadedTeaPreferences: TeaPreferencesTable? = teaDb.teaPreferencesDao().teaPreferences()
        return if (loadedTeaPreferences == null) {
            TeaPreferences()
        } else {
            loadedTeaPreferences.toTeaPreferences()
        }
    }

    fun savePreferences(teaPreferences: TeaPreferences) {
        GlobalScope.launch(Dispatchers.IO){
            teaDb.teaPreferencesDao().insert(TeaPreferencesTable.from(teaPreferences))
        }
    }

    private suspend fun getTeabagsFromDb() {
        suspendCoroutine<Boolean> { continuation ->
            allTeaBags = teaDb.teabagDao().allTeaBags.map { it.toTeaBag() }
            continuation.resume(allTeaBags.isEmpty())
        }
    }

    suspend fun getTimeState(): TimeState {
        return teaDb.timeStateDao().timeState(TimeState.ID).toTimeState()
    }

    fun saveTimeState(timeState: TimeState) {
        GlobalScope.launch(Dispatchers.IO) {
            teaDb.timeStateDao().insert(TimeStateTable.from(timeState))
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
                teaDb.teabagDao().insert(TeaBagTable.from(it))
            }
        }
    }

    private fun clear() {
        clearJobs()
        rxSubs.dispose()
        teaDb.invalidationTracker.removeObserver(teabagDbObserver)
    }

}