package com.jacquessmuts.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.core.model.TeaPreferences
import com.jacquessmuts.database.model.TeaPreferencesEntity
import com.jacquessmuts.database.model.TeaPreferencesEntity.Companion.PREFERENCES_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
internal interface TeaPreferencesDao {

    @Query("SELECT * from $PREFERENCES_TABLE WHERE ID = :id LIMIT 1")
    fun liveTeaPreferences(id: Int): LiveData<TeaPreferencesEntity>

    @Query("SELECT * from $PREFERENCES_TABLE WHERE ID = ${TeaPreferences.ID} LIMIT 1")
    suspend fun teaPreferences(): TeaPreferencesEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teaPreferences: TeaPreferencesEntity)

    @Query("DELETE FROM $PREFERENCES_TABLE")
    suspend fun deleteAll()
}