package com.jacquessmuts.positivitea.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blueair.api.AuthService
import com.blueair.api.ServerClient
import com.blueair.core.filterRapidClicks
import com.blueair.core.subscribeAndLogE
import com.blueair.database.TeaRepository
import com.blueair.database.TeaStrength
import com.blueair.database.getDescription
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.adapter.TeaBagAdapter
import com.jacquessmuts.positivitea.adapter.TeaBagVote
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.viewmodel.MainViewModel
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
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

        //TODO: move this into a Service
        launch {
            val (teabags, timeState) =
                ServerClient.getTeabagsFromServer(teaRepository.getTimeState())
            if (timeState != null) {
                teaRepository.saveTimeState(timeState)
            }
            teaRepository.saveTeabags(teabags)
        }

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
                    launch {
                        if (ServerClient.approveTeabag(teabagVote.teaBag)) {
                            teaRepository.deleteTeabag(teabagVote.teaBag.id)

                            showMessage(R.string.approved)
                        } else {
                            showMessage(R.string.error)
                        }
                    }

                } else {
                    launch {
                        if (ServerClient.deleteUnapprovedTeabag(teabagVote.teaBag.id)) {
                            teaRepository.deleteTeabag(teabagVote.teaBag.id)
                        } else {
                            showMessage(R.string.error)
                        }
                    }

                }
            })

        fab.setOnClickListener {

            if (AuthService.isUserLoggedIn()) {
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
            } else {
                signIn()
            }
        }

        notificationService.scheduleNextNotification()
    }

    fun showMessage(@StringRes resId: Int) {
        val toast = Toast.makeText(this@MainActivity, resId, Toast.LENGTH_LONG)
        toast.show()
    }

    fun signIn() {
        startActivity(AuthService.generateAuthIntent())
    }
}
