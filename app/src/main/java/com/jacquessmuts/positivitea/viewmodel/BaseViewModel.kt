package com.jacquessmuts.positivitea.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jacquessmuts.positivitea.PositiviTeaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import org.kodein.di.KodeinAware
import kotlin.coroutines.CoroutineContext

/**
 * Created by jacquessmuts on 2019-03-21
 * Base AndroidViewModel with Kodein and Coroutine context provided
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application),
    CoroutineScope,
    KodeinAware {

    val job: Job = Job()

    override val kodein by lazy {
        (application as PositiviTeaApp).kodein
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancelChildren()
    }
}