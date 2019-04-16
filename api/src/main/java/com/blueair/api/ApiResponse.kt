package com.blueair.api

import timber.log.Timber

/**
 * Created by jacquessmuts on 2019-04-13
 * This class represent a result of an api call. It's a wrapper similar to Result<> in
 * Kotlin-experimental.
 *
 * I'm still deciding whether this class should be internal or not.
 */
internal data class ApiResponse<T> (val value: T? = null, val error: Throwable? = null) {

    init {
        if (value == null && error == null)
            throw IllegalArgumentException("The ApiResponse class must have a value, an error, or both. Not neither.")

        if (error != null)
            Timber.e("Api call returned $error")
    }

    /**
     * No errors, result returned
     */
    val isSuccess: Boolean = (value != null && error == null)

    /**
     * Value was returned but also an error
     */
    val isPartialSuccess: Boolean = (value != null && error != null)

    /**
     * No value, plus error message
     */
    val isFailure: Boolean = (value == null && error != null)

    companion object {

        fun <T> success(value: T): ApiResponse<T> {
            return ApiResponse(value, null)
        }

        fun <T> fail(error: Throwable): ApiResponse<T> {
            return ApiResponse(null, error)
        }
    }
}