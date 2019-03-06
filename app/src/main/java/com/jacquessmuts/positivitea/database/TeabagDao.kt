package com.jacquessmuts.positivitea.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacquessmuts.positivitea.models.Teabag
import com.jacquessmuts.positivitea.models.Teabag.Companion.TEABAG_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * Provides the interface for database queries
 */
@Dao
interface TeabagDao {

    @get:Query("SELECT * from $TEABAG_TABLE ORDER BY score DESC")
    val allTeabags: List<Teabag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teabag: Teabag)

    @Query("DELETE FROM $TEABAG_TABLE")
    fun deleteAll()
}