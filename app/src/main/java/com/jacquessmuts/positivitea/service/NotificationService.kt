package com.jacquessmuts.positivitea.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jacquessmuts.core.ConversionUtils
import com.jacquessmuts.core.CoroutineService
import com.jacquessmuts.core.model.TeaBag
import com.jacquessmuts.core.model.TeaPreferences
import com.jacquessmuts.database.TeaRepository
import com.jacquessmuts.core.model.TeaStrength
import com.jacquessmuts.core.model.getWaitTimeInSeconds
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.activity.MainActivity
import com.jacquessmuts.positivitea.workmanager.NotificationWorker
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
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
class NotificationService(
    private val context: Context,
    private val teaRepository: TeaRepository
) : CoroutineService {

    companion object {
        const val CHANNEL_ID = "111"
        const val WORKER_TAG = "notification_loop"
    }

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    private var teaPreferences: TeaPreferences? = null
        set(nuPreferences) {
            field = nuPreferences
            if (nuPreferences != null) {
                teaPreferencesPublisher.onNext(nuPreferences)
            }
        }

    private val teaPreferencesPublisher: PublishSubject<TeaPreferences> by lazy {
        PublishSubject.create<TeaPreferences>()
    }

    init {
        launch(Dispatchers.IO) {
            teaPreferences = teaRepository.loadPreferences()
        }


        // TODO: do this slightly less often
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



    fun updateTeaStrength(nuStrength: TeaStrength) {

        val preferences = teaPreferences ?: TeaPreferences()

        val oldStrength = preferences.teaStrength
        teaPreferences = preferences.copy(teaStrength = nuStrength, previousStrength = oldStrength)
        Timber.d("Saving teaPreferences as $teaPreferences")

        teaPreferences?.let { teaRepository.savePreferences(it) }
    }

    fun scheduleNextNotification() {

        launch {

            while (teaPreferences == null) {
                delay(10)
                Timber.w("delaying notification service by 10ms because of a race condition")
            }
            if (teaPreferences == null)
                throw IllegalStateException("How did this even happen?")

            val preferences = teaPreferences!!

            val oldTag = "$WORKER_TAG ${preferences.previousStrength.strength}"
            val tag = "$WORKER_TAG ${preferences.teaStrength.strength}"
            val delay = ConversionUtils.getRandomizedTime(preferences.teaStrength.getWaitTimeInSeconds())
            Timber.d("scheduling next notification; oldTag $tag, \ntag $tag, \ntime $delay")

            if (tag != oldTag) {
                // Cancel the previously scheduled notification
                WorkManager.getInstance().cancelAllWorkByTag(oldTag)
            }

            val notificationWorkRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .addTag(tag)
                    .build()

            // enqueueUniqueWork guarantees that identical work is not overwritten or doubled upon
            WorkManager.getInstance().enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.KEEP,
                notificationWorkRequest)
        }
    }

    fun showRandomNotification() {

        Timber.v("showing random notification")

        launch {

            while (teaRepository.allTeaBags.isEmpty()) {
                Timber.w("Waiting for teabags to be loaded from server/db")
                delay(100)
            }

            val teabagNumber = teaRepository.allTeaBags.size
            if (teabagNumber < 1)
                this.coroutineContext.cancel()

            val selection = Random.nextInt(0, teabagNumber)
            showNotification(teaRepository.allTeaBags[selection])
        }
    }

    private fun showNotification(teaBag: TeaBag) {
        Timber.d("Showing notification for $teaBag")

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(teaBag.title)
            .setContentText(teaBag.message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(teaBag.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            var notificationId = 0
            teaBag.id.forEach {
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