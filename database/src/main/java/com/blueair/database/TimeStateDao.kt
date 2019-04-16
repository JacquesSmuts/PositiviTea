package com.blueair.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueair.database.TimeState.Companion.TIMESTATE_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TimeStateDao {

    @WorkerThread
    @Query("SELECT * from $TIMESTATE_TABLE WHERE ID = ${TimeState.ID} LIMIT 1")
    suspend fun timeState(): TimeState

    @get:Query("SELECT * from $TIMESTATE_TABLE WHERE ID = ${TimeState.ID} LIMIT 1")
    val liveTimeState: LiveData<TimeState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timeState: TimeState)

    @Query("DELETE FROM $TIMESTATE_TABLE")
    fun deleteAll()
}