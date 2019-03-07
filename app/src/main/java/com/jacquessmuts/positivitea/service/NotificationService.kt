package com.jacquessmuts.positivitea.service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.database.TeaDb
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.model.Teabag
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

/**
 * Created by jacquessmuts on 2019-03-07
 * This service schedules notifications, and schedules itself to wake up to schedule more notifications
 */
class NotificationService(private val context: Context,
                          private val db: TeaDb,
                          private val teaService: TeaService): CoroutineService {

    companion object {
        const val CHANNEL_ID = "111"
    }

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    var teaPreferences: TeaPreferences = TeaPreferences()

    fun showRandomNotification() {
        launch {

            if (teaService.allTeaBags.isEmpty()){
                delay(2000)
            }


            val teabagNumber = teaService.allTeaBags.size-1
            if (teabagNumber < 0)
                this.coroutineContext.cancel()

            val selection = Random.nextInt(0, teabagNumber)
            showNotification(teaService.allTeaBags[selection])
        }
    }

    private fun showNotification(teabag: Teabag) {
        Timber.d("Showing notification $teabag")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(teabag.title)
            .setContentText(teabag.message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(teabag.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            var notificationId = 0
            teabag.id.forEach {
                notificationId += it.toInt()
            }
            notify(notificationId, notification)
        }
    }

    private fun clear() {
        clearJobs()
        rxSubs.dispose()
    }

}