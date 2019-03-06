package com.jacquessmuts.positivitea.database

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jacquessmuts.positivitea.CoroutineService
import com.jacquessmuts.positivitea.firestore.FirestoreConstants
import com.jacquessmuts.positivitea.models.Teabag
import kotlinx.coroutines.launch
import timber.log.Timber

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

    fun getTeabags() {

        val db = FirebaseFirestore.getInstance()

        db.collection(FirestoreConstants.COLLECTION_TEABAGS)
            .get()
            .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                override fun onComplete(task: Task<QuerySnapshot>) {
                    if (task.isSuccessful() && task.getResult() != null) {

                        val nuTeaBags = mutableListOf<Teabag>()
                        for (document in task.getResult()!!) {
                            nuTeaBags.add(Teabag(
                                id = document.getString(FirestoreConstants.FIELD_ID) ?: "",
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