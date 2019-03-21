package com.jacquessmuts.positivitea.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.jacquessmuts.positivitea.model.TeaBag
import com.jacquessmuts.positivitea.model.TeaPreferences
import com.jacquessmuts.positivitea.service.TeaRepository
import org.kodein.di.generic.instance

/**
 * Created by jacquessmuts on 2019-03-21
 * Viewmodel for main activity
 */
class MainViewModel(application: Application) : BaseViewModel(application) {

    private val teaRepository: TeaRepository by instance()

    val allTeaBags: LiveData<List<TeaBag>> = teaRepository.liveTeaBags

    val teaPreferences: LiveData<TeaPreferences> = teaRepository.liveTeaPreferences
}