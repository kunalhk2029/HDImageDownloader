package com.app.imagedownloader.framework.RemoteConfig

import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.Utils.Logger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class RemoteConfig
@Inject
constructor(private val sharedPrefRepository: SharedPrefRepository) {

    private val remoteConfig = Firebase.remoteConfig

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    fun init() {
        remoteConfig.setConfigSettingsAsync(configSettings)
        val map: Map<String, Any> =
            mapOf("adsDisabled" to true)
        remoteConfig.setDefaultsAsync(map)
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful){
                remoteConfig.get("adsDisabled").asBoolean().let {
                    Logger.log("Debug RConfig AdsServiceProvider string  = " + it)
                    sharedPrefRepository.set_DisableAdsAndPromo(it)
                }
            }
        }
    }
}