package com.jacquessmuts.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.database.TeaBag.Companion.TEABAG_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TeabagDao {

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val allTeaBags: List<TeaBag>

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val liveAllTeaBags: LiveData<List<TeaBag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teaBag: TeaBag)

    @Query("DELETE FROM $TEABAG_TABLE WHERE ID = :id")
    fun delete(id: String)

    @Query("DELETE FROM $TEABAG_TABLE")
    fun deleteAll()
}