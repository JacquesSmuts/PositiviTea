package com.jacquessmuts.positivitea.models

import java.util.*

/**
 * Created by jacquessmuts on 2019-03-07
 * This is a State which determines how often each service should do API calls.
 */

data class TimeState(val timeTeabagsUpdated: Long = System.currentTimeMillis()) {

    companion object {
        const val MINIMUM_TIME_SINCE_TEA_UPDATE = 24 * 60 * 60 * 1000 // 24 hours
    }

    val canMakeNewApiCall: Boolean
        get() {
            return true//(System.currentTimeMillis() - timeTeabagsUpdated) > MINIMUM_TIME_SINCE_TEA_UPDATE
        }

}

val TimeState.dateTeabagsUpdated: Date
    get() = Date(timeTeabagsUpdated)
