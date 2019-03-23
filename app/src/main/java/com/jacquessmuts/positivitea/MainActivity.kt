package com.jacquessmuts.positivitea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jacquessmuts.positivitea.model.TeaStrength
import com.jacquessmuts.positivitea.model.getDescription
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import com.jacquessmuts.positivitea.viewmodel.MainViewModel
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), KodeinAware {

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    override val kodein by org.kodein.di.android.kodein()

    private lateinit var mainViewModel: MainViewModel

    private val notificationService: NotificationService by instance()

    val adapter by lazy { TeaBagAdapter(this) }

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

        notificationService.scheduleNextNotification()
    }

    override fun onPause() {
        rxSubs.clear()
        super.onPause()
    }
}
