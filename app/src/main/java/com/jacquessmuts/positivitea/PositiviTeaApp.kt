package com.jacquessmuts.positivitea

import android.app.Application
import timber.log.Timber

/**
 * Created by jacquessmuts on 2018/10/05
 * TODO: Add a class header comment!
 */
class PositiviTeaApp: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}