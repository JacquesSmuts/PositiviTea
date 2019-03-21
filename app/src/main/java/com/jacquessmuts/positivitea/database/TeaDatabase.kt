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
abstract class TeaDatabase : RoomDatabase() {

    abstract fun teabagDao(): TeabagDao

    abstract fun timeStateDao(): TimeStateDao

    abstract fun teaPreferencesDao(): TeaPreferencesDao

    companion object {

        var INSTANCE: TeaDatabase? = null

        fun getDatabase(context: Context): TeaDatabase {
            if (INSTANCE == null) {
                synchronized(TeaDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        TeaDatabase::class.java,
                        DATABASE_NAME)
                        .addCallback(TeaDatabaseCallback())
                        .build()
                }
            }
            return INSTANCE!!
        }

        const val DATABASE_NAME = "tea_keeper.db"
    }
}