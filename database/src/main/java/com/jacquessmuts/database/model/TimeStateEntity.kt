package com.jacquessmuts.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jacquessmuts.core.model.TimeState
import com.jacquessmuts.database.model.TimeStateEntity.Companion.TIMESTATE_TABLE

/**
 * Created by jacquessmuts on 2019-03-07
 * The table for TimeState
 */
@Entity(tableName = TIMESTATE_TABLE)
internal data class TimeStateEntity(
    @PrimaryKey val id: Int,
    val timeTeabagsUpdated: Long) {

    companion object {
        const val TIMESTATE_TABLE = "timestate_table"

        fun from(timeState: TimeState): TimeStateEntity {
            return TimeStateEntity(timeState.id, timeState.timeTeabagsUpdated)
        }
    }

    fun toTimeState(): TimeState {
        return TimeState(id, timeTeabagsUpdated)
    }
}