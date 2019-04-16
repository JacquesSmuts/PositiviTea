package com.blueair.core

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by jacquessmuts on 2018/09/30
 * This is for custom rxJava extensions
 */

// adds a standard ±300ms delay for clickableObjects
fun <T> Observable<T>.filterRapidClicks() = throttleFirst(1000, TimeUnit.MILLISECONDS)

// same as subscribe, except it logs errors with Timber.e() automatically
fun <T> Observable<T>.subscribeAndLogE(onNext: (it: T) -> Unit): Disposable =
        subscribe({ onNext(it) }, Timber::e)

class ErrorConsumer<T> : Consumer<T> {
    override fun accept(t: T) {
        if (t is Throwable)
            Timber.e(t)
    }
}

/**
 * Puts the Observer on the Main/UI Thread using [Observable.observeOn]. Please make sure you understand
 * the difference between [Observable.subscribeOn] and [Observable.observeOn] when using this.
 */
fun <T> Observable<T>.uiThread() = observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.backgroundThread() = observeOn(Schedulers.computation())

fun <T> Single<T>.uiThread() = observeOn(AndroidSchedulers.mainThread())
