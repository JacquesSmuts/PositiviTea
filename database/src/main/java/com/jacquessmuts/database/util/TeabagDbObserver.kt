package com.jacquessmuts.database.util

import androidx.room.InvalidationTracker
import com.jacquessmuts.database.dao.TeabagDao
import com.jacquessmuts.database.model.TeaBagTable

import io.reactivex.subjects.PublishSubject

internal class TeabagDbObserver(
    private val teabagDao: TeabagDao,
    val teaBagPublisher: PublishSubject<List<TeaBagTable>>
) :
    InvalidationTracker.Observer(TeaBagTable.TEABAG_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val teabags = teabagDao.allTeaBags
        teaBagPublisher.onNext(teabags)
    }
}