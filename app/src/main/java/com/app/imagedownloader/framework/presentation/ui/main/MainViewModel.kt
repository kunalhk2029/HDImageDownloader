package com.app.imagedownloader.framework.presentation.ui.main

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.RemoteConfig.RemoteConfig
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.WorkManagers.DeleteGlideCacheWorker
import com.app.imagedownloader.framework.WorkManagers.WorkerDeleteShared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val sharedPrefRepository: SharedPrefRepository,
    val generalAdsManager: GeneralAdsManager,
    private val remoteConfig: RemoteConfig,
    private val workManager: WorkManager
) : ViewModel() {

    val permissionGranted: Channel<Boolean> = Channel()

    companion object{
        var appThemeChannel: Channel<Int> = Channel()
    }

    init {
        CoroutineScope(IO).launch {
            launch(IO) {
                remoteConfig.init()
            }
            launch(IO) {
                sharedPrefRepository.setRatingParams(true)
            }
        }
    }

    fun set_FCM_COMMON_SUBSCRIBED(boolean: Boolean) {
        sharedPrefRepository.set_FCM_COMMON_SUBSCRIBED(boolean)
    }

    fun get_FCM_COMMON_SUBSCRIBED(): Boolean {
        return sharedPrefRepository.get_FCM_COMMON_SUBSCRIBED()
    }

    fun getAppTheme(): Int {
        return sharedPrefRepository.getTheme()
    }

    private fun enqueueGlideCacheClearWorker() {
        val req = OneTimeWorkRequestBuilder<DeleteGlideCacheWorker>()
            .build()
        workManager.enqueue(req)
    }
}