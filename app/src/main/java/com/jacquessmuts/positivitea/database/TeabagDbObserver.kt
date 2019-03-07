package com.jacquessmuts.positivitea.database

import androidx.room.InvalidationTracker

import com.jacquessmuts.positivitea.model.Teabag

import io.reactivex.subjects.PublishSubject

internal class TeabagDbObserver(private val teabagDao: TeabagDao,
                                val teabagPublisher: PublishSubject<List<Teabag>>) :
    InvalidationTracker.Observer(Teabag.TEABAG_TABLE) {

    override fun onInvalidated(tables: Set<String>) {
        val teabags = teabagDao.allTeabags
        teabagPublisher.onNext(teabags)
    }
}