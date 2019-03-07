package com.jacquessmuts.positivitea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.service.TeaService
import com.jacquessmuts.positivitea.util.subscribeAndLogE
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber

class MainActivity : AppCompatActivity(), KodeinAware {

    protected val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    override val kodein by org.kodein.di.android.kodein()

    val teaService: TeaService by instance()
    val notificationService: NotificationService by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        Timber.i("started with ${teaService.allTeaBags.size}")
        rxSubs.add(teaService.teabagObservable.subscribeAndLogE {
            Timber.i("Updated with ${teaService.allTeaBags.size}")
        })

        notificationService.showRandomNotification()
    }

    override fun onPause() {
        rxSubs.clear()
        super.onPause()
    }

}
