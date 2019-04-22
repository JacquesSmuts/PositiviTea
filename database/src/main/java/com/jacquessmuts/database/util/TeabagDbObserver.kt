package com.jacquessmuts.database.util

import androidx.room.InvalidationTracker
import com.jacquessmuts.database.dao.TeabagDao
import com.jacquessmuts.database.model.TeaBagEntity

import io.reactivex.subjects.PublishSubject

internal class TeabagDbObserver(
    private val teabagDao: TeabagDao,
    val teaBagPublisher: PublishSubject<List<TeaBagEntity>>
) :
    InvalidationTracker.Observer(TeaBagEntity.TEABAG_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val teabags = teabagDao.allTeaBags
        teaBagPublisher.onNext(teabags)
    }
}