package com.jacquessmuts.positivitea.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jacquessmuts.positivitea.PositiviTeaApp
import com.jacquessmuts.positivitea.service.NotificationService
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * This class is used by the Android WorkManager to show a notification and schedule the next one.
 */
class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams), KodeinAware {

    override val kodein by lazy { (applicationContext as PositiviTeaApp).kodein }

    val notificationService: NotificationService by instance()

    override fun doWork(): Result {

        notificationService.showRandomNotification()

        notificationService.scheduleNextNotification()

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
    }
}