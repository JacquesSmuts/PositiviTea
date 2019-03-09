package com.jacquessmuts.positivitea.database

import androidx.room.InvalidationTracker

import com.jacquessmuts.positivitea.model.TeaBag

import io.reactivex.subjects.PublishSubject

internal class TeabagDbObserver(private val teabagDao: TeabagDao,
                                val teaBagPublisher: PublishSubject<List<TeaBag>>) :
    InvalidationTracker.Observer(TeaBag.TEABAG_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val teabags = teabagDao.allTeaBags
        teaBagPublisher.onNext(teabags)
    }
}