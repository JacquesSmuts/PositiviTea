package com.jacquessmuts.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jacquessmuts.core.AppUtils
import com.jacquessmuts.database.TeaBag
import com.jacquessmuts.database.TimeState
import timber.log.Timber
import java.util.UUID
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
    suspend fun getTeabagsFromServer(timeState: TimeState, forceLoad: Boolean = false): Pair<List<TeaBag>, TimeState?> {

        Timber.v("Considering getting teabags from server")

        // There might be a race condition here where timeState could be null on first startup, hence == true
        val hours = RemoteConfig.get<Long>("hours_between_updates")
        if (!(timeState.canMakeNewApiCall(RemoteConfig.get("hours_between_updates")) == true || forceLoad))
            return Pair(listOf(), null)

        Timber.d("Getting teabags from server")

        val collection = if (AppUtils.isModerator()) {
            FirestoreConstants.COLLECTION_UNAPPROVED_TEABAGS
        } else {
            FirestoreConstants.COLLECTION_TEABAGS
        }

        val firestore = FirebaseFirestore.getInstance()

        return suspendCoroutine { continuation ->
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

                        Timber.i("A total of ${nuTeaBags.size} Teabags downloaded")
                        continuation.resume(Pair(nuTeaBags,
                            TimeState(timeTeabagsUpdated = System.currentTimeMillis())))
                    } else {
                        Timber.e("Error getting documents. ${task.exception}")
                        continuation.resume(Pair(listOf<TeaBag>(), null))
                    }
                }
        }
    }


    suspend fun approveTeabag(approvedTeabag: TeaBag): Boolean {

        val firstSucccess = suspendCoroutine<Boolean> { continuation ->
            val docData = HashMap<String, Any?>()
            docData["title"] = approvedTeabag.title
            docData["message"] = approvedTeabag.message
            docData["score"] = approvedTeabag.score

            Timber.i("Saving teabag to server. Teabag = $approvedTeabag")

            FirebaseFirestore.getInstance()
                .collection(FirestoreConstants.COLLECTION_TEABAGS)
                .document(approvedTeabag.id)
                .set(docData, SetOptions.merge())
                .addOnCompleteListener { result ->
                    Timber.d("Approved teabag. Success = ${result.isSuccessful}")
                    continuation.resume(result.isSuccessful)
                }
        }

        if (firstSucccess) {
            deleteUnapprovedTeabag(approvedTeabag.id)
        }
        return firstSucccess
    }

    suspend fun deleteUnapprovedTeabag(id: String): Boolean {

        return suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(FirestoreConstants.COLLECTION_UNAPPROVED_TEABAGS)
                .document(id)
                .delete()
                .addOnCompleteListener { result ->
                    continuation.resume(result.isSuccessful)

                    Timber.d("Saved teabag. Success = ${result.isSuccessful}")
                }
        }
    }

    suspend fun saveUnapprovedTeaBagToServer(title: String, message: String): Boolean {
        require(title.isNotBlank())
        require(message.isNotBlank())

        val nuTeabag = TeaBag(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            score = 0L
        )

        val docData = HashMap<String, Any?>()
        docData["title"] = nuTeabag.title
        docData["message"] = nuTeabag.message
        docData["score"] = nuTeabag.score

        Timber.i("Saving teabag to server. Teabag = $nuTeabag")

        return suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(FirestoreConstants.COLLECTION_UNAPPROVED_TEABAGS)
                .document(nuTeabag.id)
                .set(docData, SetOptions.merge())
                .addOnCompleteListener { result ->
                    Timber.d("Saved teabag. Success = ${result.isSuccessful}")
                    continuation.resume(result.isSuccessful)

                }
        }
    }
}