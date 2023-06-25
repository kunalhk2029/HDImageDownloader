package com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.interactors.favPhotos.GetFavPhotos
import com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview.state.FavPhotosPreviewStateEvents
import com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview.state.FavPhotosPreviewViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FavPhotosPreviewViewModel @Inject constructor(
    private val GetFavPhotos: GetFavPhotos
) : ViewModel() {

    private val _favPhotosPreviewViewState: MutableLiveData<FavPhotosPreviewViewState> =
        MutableLiveData()
    val favPhotosPreviewViewState: LiveData<FavPhotosPreviewViewState> = _favPhotosPreviewViewState

    private val _favPhotosPreviewDataState: MutableLiveData<DataState<FavPhotosPreviewViewState>> =
        MutableLiveData()
    val favPhotosPreviewDataState: LiveData<DataState<FavPhotosPreviewViewState>> =
        _favPhotosPreviewDataState

    init {
        onEvent(FavPhotosPreviewStateEvents.getFavPhotos)
    }

    fun onEvent(favPhotosPreviewStateEvents: FavPhotosPreviewStateEvents) {
        when (favPhotosPreviewStateEvents) {
            FavPhotosPreviewStateEvents.getFavPhotos -> {
                GetFavPhotos.execute().onEach {
                    _favPhotosPreviewDataState.value = it
                }.launchIn(viewModelScope)
            }
        }
    }

    fun setFavPhotosList(list: List<FavPhotos>) {
        val currentViewState = getFavPhotosPreviewViewState()
        currentViewState.list = list
        _favPhotosPreviewViewState.value = currentViewState
    }

    fun getFavPhotosPreviewViewState(): FavPhotosPreviewViewState {
        return _favPhotosPreviewViewState.value ?: FavPhotosPreviewViewState()
    }
}