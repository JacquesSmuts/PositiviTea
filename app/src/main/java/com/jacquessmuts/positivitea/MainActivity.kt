package com.jacquessmuts.positivitea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    var messages: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()

        db.collection("messages")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            val nuMessages = mutableListOf<String>()
                            for (document in task.getResult()!!) {
                                nuMessages.add(document.getString("message") ?: "")
                            }
                            messages = nuMessages
                            textView.setText(messages[0])

                            storeMessagesLocally()
                            scheduleNotifications()
                        } else {
                            Timber.w("Error getting documents. ${task.exception}")
                        }
                    }

                })

    }

    private fun storeMessagesLocally(){
        //TODO
    }

    private fun scheduleNotifications(){

    }
}
