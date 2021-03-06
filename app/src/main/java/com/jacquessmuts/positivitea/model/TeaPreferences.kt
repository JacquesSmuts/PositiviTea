package com.jacquessmuts.positivitea.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jacquessmuts.positivitea.model.TeaPreferences.Companion.PREFERENCES_TABLE

/**
 * Created by jacquessmuts on 2019-03-07
 * This is a State which determines how often each service should do API calls.
 */
@Entity(tableName = PREFERENCES_TABLE)
data class TeaPreferences(
    @PrimaryKey val id: Int = ID,
    val teaStrength: TeaStrength = TeaStrength(),
    val previousStrength: TeaStrength = teaStrength
) {

    companion object {
        const val ID = 1600
        const val PREFERENCES_TABLE = "preferences_table"
    }
}
