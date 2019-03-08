package com.jacquessmuts.positivitea.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.database.TeaDb
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.model.Teabag
import com.jacquessmuts.positivitea.util.ConversionUtils
import com.jacquessmuts.positivitea.workmanager.NotificationWorker
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Created by jacquessmuts on 2019-03-07
 * This service schedules notifications, and schedules itself to wake up to schedule more notifications
 */
class NotificationService(private val context: Context,
                          private val teaService: TeaService): CoroutineService {

    companion object {
        const val CHANNEL_ID = "111"
        const val WORKER_TAG = "notification_loop"
    }

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    var teaPreferences: TeaPreferences = TeaPreferences()

    init {
        //TODO: do this slightly less often
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRandomNotification() {

        Timber.v("showing random notification")

        launch {

            if (teaService.allTeaBags.isEmpty()){
                // find a better way to wait for the list to be finished loaded
                delay(2000)
            }

            val teabagNumber = teaService.allTeaBags.size
            if (teabagNumber < 1)
                this.coroutineContext.cancel()

            val selection = Random.nextInt(0, teabagNumber)
            showNotification(teaService.allTeaBags[selection])
        }
    }


    fun scheduleNextNotification() {

        Timber.d("scheduling next notification")

        // First check if there isn't already a notification scheduled. If so, cancel it.
        WorkManager.getInstance().getWorkInfosByTag(WORKER_TAG).cancel(false)

        val notificationWorkRequest =
            OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(ConversionUtils.getRandomizedTime(60), TimeUnit.SECONDS)
                .addTag(WORKER_TAG)
                .build()

        WorkManager.getInstance().enqueue(notificationWorkRequest)
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