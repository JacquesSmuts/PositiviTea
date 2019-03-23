package com.jacquessmuts.positivitea.activity

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.kodein.di.KodeinAware
import kotlin.coroutines.CoroutineContext

/**
 * Created by jacquessmuts on 2019-03-23
 * Base for other activites, mostly to provide kodein and coroutine functionality
 */
abstract class BaseActivity: AppCompatActivity(), KodeinAware, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    protected val rxSubs: CompositeDisposable by lazy { CompositeDisposable() }

    override val kodein by org.kodein.di.android.kodein()

    override fun onPause() {
        rxSubs.clear()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

}