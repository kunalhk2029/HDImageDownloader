package com.app.imagedownloader.framework.InAppUpdatesManager

import android.annotation.SuppressLint
import android.content.Context
import com.app.imagedownloader.framework.Utils.Logger
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdatesManager
@Inject constructor(val context: Context) {

    val appUpdateManager = AppUpdateManagerFactory.create(context)

    suspend fun initProductionUpdate(): Boolean? {
        // false for flexible
        // true for immediate
        var showCustomUpdateUi: Boolean? = null
        checkForUpdatesProductionByInAppApi().let {
            if (it == 2) {
                val priority = checkUpdatePriority()
                if (priority == 4 || priority == 5) {
                    showCustomUpdateUi = true
//                    startImmediateProductionUpdate(activity)
                } else if (priority != -1) {
                    showCustomUpdateUi = false
                }
            }
        }
        return showCustomUpdateUi
    }

    @SuppressLint("SwitchIntDef")
    suspend fun checkForUpdatesProductionByInAppApi(): Int {
        /**
        check with google Api
        0 Error
        1 Not Available
        2 Available
         **/
        val job = Job()
        var updateStatus = 0
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful) {
//                 Logger.log("5965952+95955 = "+it.result.availableVersionCode())
                when (it.result.updateAvailability()) {
                    1 -> {
                        updateStatus = 1
                    }
                    2 -> {
                        updateStatus = 2
                    }
                }
                job.complete()
            } else {
                job.complete()
            }
        }
        while (!job.isCompleted) {
            delay(1000L)
        }
        return updateStatus
    }

    suspend fun checkUpdatePriority(): Int {
        val job = Job()
        var updatePriority = -1

        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful) {
                updatePriority = it.result.updatePriority()
                job.complete()
            } else {
                job.complete()
            }
        }
        while (!job.isCompleted) {
            delay(1000L)
        }
        return updatePriority
    }

    suspend fun extractIFdecider(vc: String?): Int? {
//        Sfinder.IFdecider.Major.Minor.Patch//20.29.0.1.6
//        val tempvc ="20.29.1.1.0.6"
        val tempvc = vc
        var fif: Int? = null
        try {
            if (tempvc != null) {
                val finishdotind = tempvc.substring(6).indexOf(".")
                val finalif = tempvc.substring(6, (6 + finishdotind))
                fif = finalif.toInt()
                Logger.log("Debug 454844564565 IFdecider......... = " + finalif)
            }
        } catch (e: Exception) {
            fif = null
        }
        return fif
    }
}