package com.app.imagedownloader.framework.presentation.ui

import com.app.imagedownloader.framework.presentation.ui.main.Payment.Payment.Companion.initQonversionSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@HiltAndroidApp
class Application:android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        initQonversionSdk.receiveAsFlow().onEach {
            initQonversionSdk(this)
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }
}