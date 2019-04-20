package com.jacquessmuts.core

/**
 * Created by jacquessmuts on 2019-03-24
 * Utils class for determine app configurations and types
 */
object AppUtils {

    fun isDebug() = BuildConfig.DEBUG

    fun isModerator() = BuildConfig.FLAVOR == "moderator"

    fun isUser() = BuildConfig.FLAVOR == "user"
}