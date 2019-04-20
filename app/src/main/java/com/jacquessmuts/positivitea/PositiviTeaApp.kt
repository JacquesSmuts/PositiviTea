package com.jacquessmuts.positivitea

import android.app.Application
import android.content.Context
import com.jacquessmuts.api.RemoteConfig
import com.jacquessmuts.database.TeaRepository
import com.jacquessmuts.positivitea.service.NotificationService
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
        bind<com.jacquessmuts.database.TeaDatabase>() with singleton { com.jacquessmuts.database.TeaDatabase.getDatabase(applicationContext) }
        bind<TeaRepository>() with eagerSingleton {
            TeaRepository(
                instance()
            )
        }
        bind<NotificationService>() with singleton { NotificationService(applicationContext, instance()) }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        RemoteConfig.setDefaults()
    }

}