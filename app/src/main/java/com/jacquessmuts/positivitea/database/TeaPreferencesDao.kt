package com.jacquessmuts.positivitea.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.model.TeaPreferences.Companion.PREFERENCES_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TeaPreferencesDao {

    @get:Query("SELECT * from $PREFERENCES_TABLE WHERE ID = ${TeaPreferences.ID} LIMIT 1")
    val liveTeaPreferences: LiveData<TeaPreferences>

    @get:Query("SELECT * from $PREFERENCES_TABLE WHERE ID = ${TeaPreferences.ID} LIMIT 1")
    val teaPreferences: TeaPreferences

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teaPreferences: TeaPreferences)

    @Query("DELETE FROM $PREFERENCES_TABLE")
    fun deleteAll()
}