package com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.business.interactors.downloadsPreview.GetDownloadedMediaFromOfflineStorage
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.state.DownloadsPreviewStateEvents
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.state.DownloadsPreviewViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DownloadsPreviewViewModel @Inject constructor(
    private val getDownloadedMediaFromOfflineStorage: GetDownloadedMediaFromOfflineStorage,
) : ViewModel() {

    private val _downloadsPreviewViewState: MutableLiveData<DownloadsPreviewViewState> =
        MutableLiveData()
    val downloadsPreviewViewState: LiveData<DownloadsPreviewViewState> = _downloadsPreviewViewState

    private val _downloadsPreviewDataState: MutableLiveData<DataState<DownloadsPreviewViewState>> =
        MutableLiveData()
    val downloadsPreviewDataState: LiveData<DataState<DownloadsPreviewViewState>> =
        _downloadsPreviewDataState

    init {
        onEvent(DownloadsPreviewStateEvents.getDownloadedMediaFromOfflineStorage)
    }

    fun onEvent(downloadsPreviewStateEvents: DownloadsPreviewStateEvents) {
        when (downloadsPreviewStateEvents) {
            DownloadsPreviewStateEvents.getDownloadedMediaFromOfflineStorage -> {
                getDownloadedMediaFromOfflineStorage().onEach {
                    _downloadsPreviewDataState.value = it
                }.launchIn(viewModelScope)
            }
        }
    }

    fun setDownloadedMediaPreviewList(list: List<DownloadedMediaInfo>) {
        val currentViewState = getDownloadsPreviewViewState()
        currentViewState.list = list
        _downloadsPreviewViewState.value = currentViewState
    }

    fun getDownloadsPreviewViewState(): DownloadsPreviewViewState {
        return _downloadsPreviewViewState.value ?: DownloadsPreviewViewState()
    }
}