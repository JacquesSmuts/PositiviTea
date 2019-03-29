package com.jacquessmuts.positivitea.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.adapter.TeaBagAdapter
import com.jacquessmuts.positivitea.adapter.TeaBagVote
import com.jacquessmuts.positivitea.model.TeaStrength
import com.jacquessmuts.positivitea.model.getDescription
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.service.TeaRepository
import com.jacquessmuts.positivitea.util.filterRapidClicks
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import com.jacquessmuts.positivitea.viewmodel.MainViewModel
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    private lateinit var mainViewModel: MainViewModel

    private val notificationService: NotificationService by instance()
    private val teaRepository: TeaRepository by instance()

    val teaBagVotePublisher: PublishSubject<TeaBagVote> by lazy {
        PublishSubject.create<TeaBagVote>()
    }

    val adapter by lazy { TeaBagAdapter(this, teaBagVotePublisher) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("OnCreate of MainActivity")
        setContentView(R.layout.activity_main)

        recyclerView.adapter = adapter

        linkViewModel()
    }


    private fun linkViewModel() {

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        mainViewModel.allTeaBags.observe(this, Observer { teaBags ->
            // Update the cached copy of the words in the adapter.
            teaBags.let { adapter.teaBags = it }
        })

        mainViewModel.teaPreferences.observe(this, Observer { preferences ->
            seekBar.progress = preferences.teaStrength.strength
            textViewRegularity.text = preferences.teaStrength.getDescription(baseContext)
        })
    }

    override fun onStart() {
        super.onStart()
        Timber.i("started with ${mainViewModel.allTeaBags.value?.size} teabags")

        rxSubs.add(RxSeekBar.changes(seekBar)
            .skip(2)
            .map { input ->
                val nuStrength = TeaStrength(input)
                textViewRegularity.text = nuStrength.getDescription(baseContext)
                nuStrength
            }
            .throttleLast(1, TimeUnit.SECONDS)
            .subscribeAndLogE { nuTeaStrength ->
                Timber.d("user adjusted strength to $nuTeaStrength")

                notificationService.updateTeaStrength(nuTeaStrength)
                notificationService.scheduleNextNotification()
        })

        rxSubs.add(teaBagVotePublisher
            .filterRapidClicks()
            .subscribeAndLogE { teabagVote ->
                if (teabagVote.isApproved) {
                    teaRepository.approveTeabag(teabagVote.teaBag) {
                        val toast = Toast.makeText(this, R.string.approved, Toast.LENGTH_LONG)
                        toast.show()
                    }
                } else {
                    teaRepository.deleteUnapprovedTeabag(teabagVote.teaBag.id) {
                        val toast = Toast.makeText(this, R.string.deleted, Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
            })

        fab.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
            } else {
                signIn()
            }
        }

        notificationService.scheduleNextNotification()
    }

    fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivity(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build())
    }
}
