package com.jacquessmuts.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

/**
 * Created by jacquessmuts on 2019-03-06
 * implement this to ensure your service (or any class) handles coroutine scope
 */
interface CoroutineService : CoroutineScope {

    val job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun clearJobs() {
        coroutineContext.cancelChildren()
    }
}