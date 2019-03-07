package com.jacquessmuts.positivitea.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.positivitea.model.TimeState
import com.jacquessmuts.positivitea.model.TimeState.Companion.TIMESTATE_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TimeStateDao {

    @get:Query("SELECT * from $TIMESTATE_TABLE WHERE ID = ${TimeState.ID} LIMIT 1")
    val timeState: TimeState

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timeState: TimeState)

    @Query("DELETE FROM $TIMESTATE_TABLE")
    fun deleteAll()
}