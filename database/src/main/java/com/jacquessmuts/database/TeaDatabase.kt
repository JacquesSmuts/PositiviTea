package com.jacquessmuts.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jacquessmuts.database.dao.TeaPreferencesDao
import com.jacquessmuts.database.dao.TeabagDao
import com.jacquessmuts.database.dao.TimeStateDao
import com.jacquessmuts.database.model.TeaBagEntity
import com.jacquessmuts.database.model.TeaPreferencesEntity
import com.jacquessmuts.database.model.TimeStateEntity
import com.jacquessmuts.database.util.Converters
import com.jacquessmuts.database.util.TeaDatabaseCallback

@Database(
    entities = [TeaBagEntity::class, TimeStateEntity::class, TeaPreferencesEntity::class],
    exportSchema = false,
    version = 2)
@TypeConverters(Converters::class)
internal abstract class TeaDatabase : RoomDatabase() {

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
                        DATABASE_NAME
                    )
                        .addCallback(TeaDatabaseCallback())
                        .build()
                }
            }
            return INSTANCE!!
        }

        const val DATABASE_NAME = "tea_keeper.db"
    }
}