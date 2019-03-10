package com.jacquessmuts.positivitea

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jacquessmuts.positivitea.model.TeaStrength
import com.jacquessmuts.positivitea.model.getDescription
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.service.TeaService
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), KodeinAware {

    protected val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    override val kodein by org.kodein.di.android.kodein()

    val teaService: TeaService by instance()
    val notificationService: NotificationService by instance()
    val teaStrength
        get() = notificationService.teaPreferences?.teaStrength

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        Timber.i("started with ${teaService.allTeaBags.size}")

        val seekbar = findViewById<SeekBar>(R.id.seekBar)
        val textViewRegularity = findViewById<TextView>(R.id.textViewRegularity)

        teaStrength?.let {
            seekbar.setProgress(it.strength)
            textViewRegularity.setText(it.getDescription(baseContext))
        }

        rxSubs.add(teaService.teabagObservable.subscribeAndLogE {
            Timber.i("teabags updated with ${teaService.allTeaBags.size} teabags")
            // Keep this here as the teaService clears itself if it is not being observed
        })

        rxSubs.add(notificationService.teaPreferencesObservable
            .subscribeAndLogE {
                seekbar.setProgress(it.teaStrength.strength)
                textViewRegularity.setText(it.teaStrength.getDescription(baseContext))
            })

        rxSubs.add(RxSeekBar.changes(seekbar)
            .skip(2)
            .map { input ->
                val nuStrength = TeaStrength(input)
                textViewRegularity.setText(nuStrength.getDescription(baseContext))
                nuStrength
            }
            .filter { input ->
                Math.abs(input.strength - (teaStrength?.strength ?: input.strength)) > 10
            }
            .throttleLast(2, TimeUnit.SECONDS)
            .subscribeAndLogE { teaStrength ->
                Timber.d("user adjusted strength to $teaStrength")

                notificationService.updateTeaStrength(teaStrength)
                notificationService.scheduleNextNotification()
        })

        notificationService.scheduleNextNotification()
    }

    override fun onPause() {
        rxSubs.clear()
        super.onPause()
    }

}
