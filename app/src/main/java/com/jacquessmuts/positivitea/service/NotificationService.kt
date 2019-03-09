package com.jacquessmuts.positivitea.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.database.TeaDb
import com.jacquessmuts.positivitea.database.TeaPreferencesDbObserver
import com.jacquessmuts.positivitea.database.TimeStateDbObserver
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.model.TeaBag
import com.jacquessmuts.positivitea.model.TeaStrength
import com.jacquessmuts.positivitea.model.TimeState
import com.jacquessmuts.positivitea.model.getWaitTimeInSeconds
import com.jacquessmuts.positivitea.util.ConversionUtils
import com.jacquessmuts.positivitea.workmanager.NotificationWorker
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
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
                          private val teaDb: TeaDb,
                          private val teaService: TeaService): CoroutineService {

    companion object {
        const val CHANNEL_ID = "111"
        const val WORKER_TAG = "notification_loop"
    }

    override val job by lazy { SupervisorJob() }

    private val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    var teaPreferences: TeaPreferences = TeaPreferences()

    private val teaPreferencesPublisher: PublishSubject<TeaPreferences> by lazy {
        PublishSubject.create<TeaPreferences>()
    }
    val teaPreferencesObservable: Observable<TeaPreferences>
        get() = teaPreferencesPublisher.hide()

    init {
        loadPreferences()

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

    fun loadPreferences() {
        launch{
            val loadedTeaPreferences: TeaPreferences? = teaDb.teaPreferencesDao().teaPreferences
            if (loadedTeaPreferences == null) {
                teaPreferences = TeaPreferences()
            } else {
                teaPreferences = loadedTeaPreferences
            }
            teaPreferencesPublisher.onNext(teaPreferences)
        }
    }

    fun updateTeaStrength(nuStrength: TeaStrength){

        teaPreferences = teaPreferences.copy(teaStrength = nuStrength)
        launch {
            teaDb.teaPreferencesDao().insert(teaPreferences)
        }

    }

    fun scheduleNextNotification() {

        val delay = ConversionUtils.getRandomizedTime(teaPreferences.teaStrength.getWaitTimeInSeconds())
        Timber.d("scheduling next notification, with time $delay")

        launch {

            // First check if there isn't already a notification scheduled. If so, cancel it.
            //TODO: this breaks the future requests as well :/ WorkManager.getInstance().getWorkInfosByTag(WORKER_TAG).cancel(false)


            val notificationWorkRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .addTag(WORKER_TAG)
                    .build()

            WorkManager.getInstance().enqueue(notificationWorkRequest)
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

    private fun showNotification(teaBag: TeaBag) {
        Timber.d("Showing notification $teaBag")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(teaBag.title)
            .setContentText(teaBag.message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(teaBag.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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