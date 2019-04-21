package com.jacquessmuts.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.database.model.TeaBagTable
import com.jacquessmuts.database.model.TeaBagTable.Companion.TEABAG_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
internal interface TeabagDao {

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val allTeaBags: List<TeaBagTable>

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val liveAllTeaBags: LiveData<List<TeaBagTable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teaBag: TeaBagTable)

    @Query("DELETE FROM $TEABAG_TABLE WHERE ID = :id")
    fun delete(id: String)

    @Query("DELETE FROM $TEABAG_TABLE")
    fun deleteAll()
}