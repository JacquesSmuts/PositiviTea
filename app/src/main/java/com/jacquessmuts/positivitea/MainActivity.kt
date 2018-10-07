package com.jacquessmuts.positivitea

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val db = FirebaseFirestore.getInstance()

        db.collection("messages")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            val messages = mutableListOf<String>()
                            for (document in task.getResult()!!) {
                                messages.add(document.getString("message") ?: "")
                            }
                            textView.setText(messages[0])
                        } else {
                            Timber.w("Error getting documents. ${task.exception}")
                        }
                    }

                })


    }
}
