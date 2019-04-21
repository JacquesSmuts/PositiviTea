package com.jacquessmuts.database.util

import android.content.Context
import com.jacquessmuts.database.TeaRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * Created by jacquessmuts on 2019-04-21
 * provides the kodein module to import
 */

object DatabaseKodeinModule {

    const val moduleName = "DeviceManager"

    fun getModule(applicationContext: Context): Kodein.Module {

        return Kodein.Module(name = moduleName) {

            // Database
            // bind<TeaDatabase>() with singleton { TeaDatabase.getDatabase(applicationContext) }
            bind<TeaRepository>() with singleton { TeaRepository(applicationContext) }
        }
    }
}
