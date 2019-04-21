package com.jacquessmuts.database.util

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jacquessmuts.core.model.TeaPreferences
import com.jacquessmuts.database.TeaDatabase
import com.jacquessmuts.database.dao.TeaPreferencesDao
import com.jacquessmuts.database.model.TeaPreferencesTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class TeaDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        GlobalScope.launch(Dispatchers.IO) {
            populateDatabase(TeaDatabase.INSTANCE!!.teaPreferencesDao())
        }
    }

    fun populateDatabase(teaPreferencesDao: TeaPreferencesDao) {
        GlobalScope.launch(Dispatchers.IO){
            teaPreferencesDao.insert(TeaPreferencesTable.from(TeaPreferences()))
        }
    }
}