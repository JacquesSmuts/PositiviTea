package com.jacquessmuts.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.database.model.TeaBagEntity
import com.jacquessmuts.database.model.TeaBagEntity.Companion.TEABAG_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
internal interface TeabagDao {

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val allTeaBags: List<TeaBagEntity>

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val liveAllTeaBags: LiveData<List<TeaBagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teaBag: TeaBagEntity)

    @Query("DELETE FROM $TEABAG_TABLE WHERE ID = :id")
    fun delete(id: String)

    @Query("DELETE FROM $TEABAG_TABLE")
    fun deleteAll()
}