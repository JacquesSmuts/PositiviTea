package com.jacquessmuts.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jacquessmuts.core.model.TeaPreferences
import com.jacquessmuts.core.model.TeaStrength
import com.jacquessmuts.database.model.TeaPreferencesEntity.Companion.PREFERENCES_TABLE

/**
 * Created by jacquessmuts on 2019-03-07
 * The table which represents TeaPreferences
 */
@Entity(tableName = PREFERENCES_TABLE)
internal data class TeaPreferencesEntity(
    @PrimaryKey val id: Int,
    val teaStrength: TeaStrength,
    val previousStrength: TeaStrength
) {

    companion object {
        const val PREFERENCES_TABLE = "preferences_table"

        fun from(pref: TeaPreferences): TeaPreferencesEntity {
            return TeaPreferencesEntity(pref.id, pref.teaStrength, pref.previousStrength)
        }
    }

    fun toTeaPreferences(): TeaPreferences {
        return TeaPreferences(id, teaStrength, previousStrength)
    }
}
