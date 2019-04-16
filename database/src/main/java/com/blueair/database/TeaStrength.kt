package com.blueair.database

import android.content.Context

/**
 * Created by jacquessmuts on 2019-03-08
 */
data class TeaStrength(val strength: Int = DEFAULT_STRENGTH) {

    companion object {
        const val DEFAULT_STRENGTH = 350
        val ACCEPTABLE_STRENGTH_RANGE = 0..1000
    }

    init {
        require(strength in ACCEPTABLE_STRENGTH_RANGE)
    }
}

/**
 * Returns a second value between 3sec and 1 week depending on input
 *
 */
fun TeaStrength.getWaitTimeInSeconds(): Long {

    return when (strength) {
        in 1000 downTo 991 -> 6 // 6 sec
        in 990 downTo 901 -> 5 * 60 // 5min
        in 900 downTo 801 -> 10 * 60 // 10min
        in 800 downTo 701 -> 30 * 60 // 30min
        in 700 downTo 601 -> 60 * 60 // 1h
        in 600 downTo 501 -> 2 * 60 * 60 // 2h
        in 500 downTo 401 -> 4 * 60 * 60 // 4h
        in 400 downTo 301 -> 8 * 60 * 60 // 8h
        in 300 downTo 201 -> 24 * 60 * 60 // 24h
        in 200 downTo 101 -> 48 * 60 * 60 // 48h
        else -> 168 * 60 * 60 // 1 week
    }
}

fun TeaStrength.getDescription(context: Context): String {

    val option = when (strength) {
        in 1000 downTo 991 -> 0 // 3 sec
        in 990 downTo 901 -> 1 // 5min
        in 900 downTo 801 -> 2 // 10min
        in 800 downTo 701 -> 3 // 30min
        in 700 downTo 601 -> 4 // 1h
        in 600 downTo 501 -> 5 // 2h
        in 500 downTo 401 -> 6 // 4h
        in 400 downTo 301 -> 7 // 8h
        in 300 downTo 201 -> 8 // 24h
        in 200 downTo 101 -> 9 // 48h
        else -> 10 // 1 week
    }

    return context.resources.getStringArray(R.array.regularity)[option]
}
