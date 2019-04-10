package com.jacquessmuts.positivitea.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blueair.api.RemoteConfig
import com.jacquessmuts.positivitea.model.TimeState.Companion.TIMESTATE_TABLE
import java.util.Date

/**
 * Created by jacquessmuts on 2019-03-07
 * This is a State which determines how often each service should do API calls.
 */
@Entity(tableName = TIMESTATE_TABLE)
data class TimeState(
    @PrimaryKey val id: Int = ID,
    val timeTeabagsUpdated: Long = System.currentTimeMillis()) {

    companion object {
        const val ID = 451
        const val TIMESTATE_TABLE = "timestate_table"
        const val ONE_HOUR = 60 * 60 * 1000 // 1 hour
    }

    val canMakeNewApiCall: Boolean
        get() {
            val hoursBetweenUpdate: Long = RemoteConfig.get("hours_between_updates")
            return (System.currentTimeMillis() - timeTeabagsUpdated) > (hoursBetweenUpdate * ONE_HOUR)
        }
}

val TimeState.dateTeabagsUpdated: Date
    get() = Date(timeTeabagsUpdated)
