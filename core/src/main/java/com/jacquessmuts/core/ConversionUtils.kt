package com.jacquessmuts.core

import kotlin.random.Random

/**
 * Created by jacquessmuts on 2019-03-08
 * Utils. For conversions.
 */
object ConversionUtils {

    /**
     * Takes given input and returns a number from 0.5 to 1.5 of that number's value
     */
    fun getRandomizedTime(seconds: Long): Long {

        val from: Long = seconds / 2
        val to: Long = (seconds * 1.5).toLong()

        return Random.nextLong(from, to)
    }
}