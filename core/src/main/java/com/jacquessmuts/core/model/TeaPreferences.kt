package com.jacquessmuts.core.model

/**
 * Created by jacquessmuts on 2019-03-07
 * This is a State which determines how often each service should do API calls.
 */
data class TeaPreferences(
    val id: Int = ID,
    val teaStrength: TeaStrength = TeaStrength(),
    val previousStrength: TeaStrength = teaStrength
) {

    companion object {
        const val ID = 1600
    }
}
