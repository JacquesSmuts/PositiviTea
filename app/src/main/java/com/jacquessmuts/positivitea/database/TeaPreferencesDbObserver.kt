package com.jacquessmuts.positivitea.database

import androidx.room.InvalidationTracker
import com.jacquessmuts.positivitea.model.TeaPreferences
import io.reactivex.subjects.PublishSubject

internal class TeaPreferencesDbObserver(private val teaPreferencesDao: TeaPreferencesDao,
                                        val teaPreferencesPublisher: PublishSubject<TeaPreferences>) :
    InvalidationTracker.Observer(TeaPreferences.PREFERENCES_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val teaPreferences = teaPreferencesDao.teaPreferences
        teaPreferencesPublisher.onNext(teaPreferences)
    }
}