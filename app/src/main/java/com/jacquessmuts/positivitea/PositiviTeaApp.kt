package com.jacquessmuts.positivitea

import android.app.Application
import android.content.Context
import com.jacquessmuts.positivitea.database.TeaDb
import com.jacquessmuts.positivitea.database.TeaService
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
class PositiviTeaApp: Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind<Application>() with instance(this@PositiviTeaApp)
        bind<Context>() with instance(applicationContext)
        bind<TeaDb>() with singleton { TeaDb.initDb(applicationContext) }
        bind<TeaService>() with eagerSingleton{ TeaService(instance()) }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val teaService: TeaService by instance()
        teaService.initialize()
    }
}