package com.jacquessmuts.positivitea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

/**
 * Created by jacquessmuts on 2019-03-06
 * TODO: Add a class header comment!
 */
abstract class CoroutineService: CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun clearJobs(){
        coroutineContext.cancelChildren()
    }
}