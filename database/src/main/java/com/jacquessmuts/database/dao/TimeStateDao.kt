package com.jacquessmuts.database.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.database.model.TimeStateTable
import com.jacquessmuts.database.model.TimeStateTable.Companion.TIMESTATE_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
internal interface TimeStateDao {

    @WorkerThread
    @Query("SELECT * from $TIMESTATE_TABLE WHERE ID = :id LIMIT 1")
    suspend fun timeState(id: Int): TimeStateTable

    @Query("SELECT * from $TIMESTATE_TABLE WHERE ID = :id LIMIT 1")
    fun liveTimeState(id: Int): LiveData<TimeStateTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timeState: TimeStateTable)

    @Query("DELETE FROM $TIMESTATE_TABLE")
    fun deleteAll()
}