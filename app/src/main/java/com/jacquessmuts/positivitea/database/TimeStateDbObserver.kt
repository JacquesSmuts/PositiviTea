package com.jacquessmuts.positivitea.database

import androidx.room.InvalidationTracker

import com.jacquessmuts.positivitea.database.TeabagDao
import com.jacquessmuts.positivitea.models.Teabag
import com.jacquessmuts.positivitea.models.TimeState
import io.reactivex.Observable

import io.reactivex.subjects.PublishSubject

internal class TimeStateDbObserver(private val timeStateDao: TimeStateDao,
                                   val timeStatePublisher: PublishSubject<TimeState>) :
    InvalidationTracker.Observer(TimeState.TIMESTATE_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val timeState = timeStateDao.timeState
        timeStatePublisher.onNext(timeState)
    }
}