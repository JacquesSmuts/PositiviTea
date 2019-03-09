package com.jacquessmuts.positivitea.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jacquessmuts.positivitea.model.TeaBag
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.model.TimeState

@Database(
    entities = [TeaBag::class, TimeState::class, TeaPreferences::class],
    exportSchema = false,
    version = 1)
@TypeConverters(Converters::class)
abstract class TeaDb : RoomDatabase() {

    abstract fun teabagDao(): TeabagDao

    abstract fun timeStateDao(): TimeStateDao

    abstract fun teaPreferencesDao(): TeaPreferencesDao

    companion object {

        const val DATABASE_NAME = "tea_keeper.db"

        fun initDb(context: Context): TeaDb {
            return Room.databaseBuilder(
                context,
                TeaDb::class.java,
                DATABASE_NAME)
                .build()
        }

    }


}