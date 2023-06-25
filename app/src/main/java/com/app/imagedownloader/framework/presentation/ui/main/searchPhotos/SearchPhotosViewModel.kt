package com.app.imagedownloader.framework.presentation.ui.main.searchPhotos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.interactors.searchPhotos.GetAutoCompleteRelatedSearchKeywords
import com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state.SearchPhotosStateEvents
import com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state.SearchPhotosViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchPhotosViewModel
@Inject constructor(
    private val getAutoCompleteRelatedSearchKeywords: GetAutoCompleteRelatedSearchKeywords
) : ViewModel() {

    private val _searchPhotosViewState: MutableLiveData<SearchPhotosViewState> = MutableLiveData()
    val searchPhotosViewState: LiveData<SearchPhotosViewState> = _searchPhotosViewState

    private val _searchPhotosDataState: MutableLiveData<DataState<SearchPhotosViewState>> =
        MutableLiveData()
    val searchPhotosDataState: LiveData<DataState<SearchPhotosViewState>> = _searchPhotosDataState


    fun onEvent(searchPhotosStateEvents: SearchPhotosStateEvents) {
        when (searchPhotosStateEvents) {
            is SearchPhotosStateEvents.autoCompleteSearchKeywordClicked -> {

            }
            is SearchPhotosStateEvents.searchBarTextChanged -> {
                getAutoCompleteRelatedSearchKeywords(searchPhotosStateEvents.keyword,).onEach {
                    _searchPhotosDataState.value=it
                }.launchIn(viewModelScope)
            }
            is SearchPhotosStateEvents.searchInitiated -> {

            }
        }
    }

    fun setautoCompletedRelatedSearchKeywordsList(list: List<String>) {
        val currentViewState = getSearchPhotosViewState()
        currentViewState.autoCompletedRelatedSearchKeywords = list
        _searchPhotosViewState.value = currentViewState
    }

    fun getSearchPhotosViewState(): SearchPhotosViewState {
        return _searchPhotosViewState.value ?: SearchPhotosViewState()
    }
}