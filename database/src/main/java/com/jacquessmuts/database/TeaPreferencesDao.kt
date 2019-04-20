package com.jacquessmuts.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.database.TeaPreferences.Companion.PREFERENCES_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TeaPreferencesDao {

    @get:Query("SELECT * from $PREFERENCES_TABLE WHERE ID = ${TeaPreferences.ID} LIMIT 1")
    val liveTeaPreferences: LiveData<TeaPreferences>

    @Query("SELECT * from $PREFERENCES_TABLE WHERE ID = ${TeaPreferences.ID} LIMIT 1")
    suspend fun teaPreferences(): TeaPreferences

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teaPreferences: TeaPreferences)

    @Query("DELETE FROM $PREFERENCES_TABLE")
    suspend fun deleteAll()
}