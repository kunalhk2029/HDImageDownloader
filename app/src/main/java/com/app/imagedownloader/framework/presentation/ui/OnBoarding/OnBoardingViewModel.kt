package com.app.imagedownloader.framework.presentation.ui.OnBoarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel

class OnBoardingViewModel : ViewModel() {
    val skipListener: Channel<Boolean> = Channel()
    val nextListener: Channel<Boolean> = Channel()
}