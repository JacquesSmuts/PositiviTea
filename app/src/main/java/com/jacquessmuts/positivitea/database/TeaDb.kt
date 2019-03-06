package com.jacquessmuts.positivitea.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jacquessmuts.positivitea.models.Teabag

@Database(
    entities = [Teabag::class],
    exportSchema = false,
    version = 1)
abstract class TeaDb : RoomDatabase() {

    abstract fun teabagDao(): TeabagDao

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