package com.blueair.api

import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by jacquessmuts on 2019-04-10
 * Used to get and post api calls to the server (firestore)
 */
object ServerClient {


    /**
     *
     */
    suspend fun getTeabagsFromServer(forceLoad: Boolean = false) {

        Timber.v("Considering getting teabags from server")

        // There might be a race condition here where timeState could be null on first startup, hence == true
        if (!(timeState?.canMakeNewApiCall == true || forceLoad))
            return

        Timber.d("Getting teabags from server")

        val collection = if (AppUtils.isModerator()) {
            FirestoreConstants.COLLECTION_UNAPPROVED_TEABAGS
        } else {
            FirestoreConstants.COLLECTION_TEABAGS
        }

        val firestore = FirebaseFirestore.getInstance()

        suspendCoroutine<Boolean> { continuation ->
            firestore.collection(collection)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {

                        val nuTeaBags = mutableListOf<TeaBag>()
                        for (document in task.result!!) {
                            nuTeaBags.add(TeaBag(
                                id = document.id,
                                title = document.getString(FirestoreConstants.FIELD_TITLE) ?: "",
                                message = document.getString(FirestoreConstants.FIELD_MESSAGE) ?: "",
                                score = document.getLong(FirestoreConstants.FIELD_SCORE) ?: 0))
                        }

                        saveTeabags(nuTeaBags)
                        timeState = TimeState(timeTeabagsUpdated = System.currentTimeMillis())
                        Timber.i("A total of ${nuTeaBags.size} Teabags downloaded")
                        continuation.resume(true)
                    } else {
                        Timber.e("Error getting documents. ${task.exception}")
                        continuation.resume(false)
                    }
                }
        }
    }
}