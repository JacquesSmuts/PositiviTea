package com.jacquessmuts.positivitea

import android.app.Application
import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jacquessmuts.positivitea.database.TeaDatabase
import com.jacquessmuts.positivitea.service.NotificationService
import com.jacquessmuts.positivitea.service.TeaRepository
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import timber.log.Timber

/**
 * Created by jacquessmuts on 2018/10/05
 * The app.
 */
class PositiviTeaApp : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind<Application>() with instance(this@PositiviTeaApp)
        bind<Context>() with instance(applicationContext)
        bind<TeaDatabase>() with singleton { TeaDatabase.getDatabase(applicationContext) }
        bind<TeaRepository>() with eagerSingleton { TeaRepository(instance()) }
        bind<NotificationService>() with singleton { NotificationService(applicationContext, instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        setRemoteConfigDefaults()
    }

    private fun setRemoteConfigDefaults() {

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)

        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        remoteConfig.fetch().addOnCompleteListener { task ->
            remoteConfig.activateFetched()
        }
    }
}