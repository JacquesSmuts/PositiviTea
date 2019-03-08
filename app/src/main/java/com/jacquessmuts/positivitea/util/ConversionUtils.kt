package com.jacquessmuts.positivitea.util

import kotlin.random.Random

/**
 * Created by jacquessmuts on 2019-03-08
 * Utils. For conversions.
 */
object ConversionUtils {

    /**
     * Returns a second value between 3sec and 1 week depending on input
     *
     * @param number must be a number in range 0..1000
     */
    fun numberToWaitSeconds(number: Int): Long {
        require(number in 0..1000)

        return when (number) {
            in 1000 downTo 991 -> 3 // 3 sec
            in 990 downTo 901 -> 5*60 // 5min
            in 900 downTo 801 -> 10*60 // 10min
            in 800 downTo 701  -> 30*60 // 30min
            in 700 downTo 601  -> 60*60 // 1h
            in 600 downTo 501  -> 2*60*60 // 2h
            in 500 downTo 401  -> 4*60*60 // 4h
            in 400 downTo 301  -> 8*60*60 // 8h
            in 300 downTo 201  -> 24*60*60 // 24h
            in 200 downTo 101  -> 48*60*60 // 48h
            else -> 168*60*60 // 1 week
        }

    }

    /**
     * Takes given input and returns a number from 0.5 to 1.5 of that number's value
     */
    fun getRandomizedTime(seconds: Long): Long {

        val from: Long = seconds/2
        val to: Long = (seconds*1.5).toLong()

        return Random.nextLong(from, to)
    }

}