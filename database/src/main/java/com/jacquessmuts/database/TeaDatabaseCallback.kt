package com.jacquessmuts.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TeaDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        GlobalScope.launch(Dispatchers.IO) {
            populateDatabase(TeaDatabase.INSTANCE!!.teaPreferencesDao())
        }
    }

    fun populateDatabase(teaPreferencesDao: TeaPreferencesDao) {
        GlobalScope.launch(Dispatchers.IO){
            teaPreferencesDao.insert(TeaPreferences())
        }
    }
}