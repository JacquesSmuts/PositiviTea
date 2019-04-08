package com.blueair.api

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Created by jacquessmuts on 2019-04-07
 * Exposes FirebaseRemoteConfig to rest of app
 */

class RemoteConfig {

    /**
     * Defaults set on app creation
     */
    fun setDefaults() {

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)

        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        remoteConfig.fetch().addOnCompleteListener { task ->
            remoteConfig.activateFetched()
        }
    }

}