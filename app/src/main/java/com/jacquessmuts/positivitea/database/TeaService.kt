package com.jacquessmuts.positivitea.database

import com.jacquessmuts.positivitea.CoroutineService
import com.jacquessmuts.positivitea.models.Teabag
import kotlinx.coroutines.launch

/**
 * Created by jacquessmuts on 2019-03-06
 * This repository/service handles DB calls, manages the db and exposes db items to the app
 */
class TeaService(val db: TeaDb): CoroutineService() {

    // TODO Add RxJava support, particularly an observer here, to make updateTeabags unnecessary

    private val teaBagDao: TeabagDao
        get() = db.teabagDao()

    var allTeaBags: List<Teabag> = listOf()
        private set

    init {
        updateTeabags()
    }

    private fun updateTeabags(){
        launch { allTeaBags = teaBagDao.allTeabags }
    }

    fun insertAll(teabags: List<Teabag>) {
        launch {
            teabags.forEach {
                teaBagDao.insert(it)
            }
            updateTeabags()
        }
    }

    fun insert(teabag: Teabag) {
        launch {
            teaBagDao.insert(teabag)
            updateTeabags()
        }
    }

}