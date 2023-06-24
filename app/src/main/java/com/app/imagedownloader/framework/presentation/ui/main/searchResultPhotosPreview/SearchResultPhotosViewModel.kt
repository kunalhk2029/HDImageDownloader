package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.imagedownloader.business.domain.DataState.DataState
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.business.interactors.singleImagePreview.FilterPhotos
import com.app.imagedownloader.business.interactors.singleImagePreview.GetPhotosFromUnsplashApi
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchResultPhotosViewModel
@Inject constructor(
    private val getPhotosFromUnsplashApi: GetPhotosFromUnsplashApi,
    private val filterPhotos: FilterPhotos,
) : ViewModel() {

    private val _searchResultPhotosPreviewViewState: MutableLiveData<SearchResultPhotosPreviewViewState> =
        MutableLiveData()
    val searchResultPhotosPreviewViewState: LiveData<SearchResultPhotosPreviewViewState> =
        _searchResultPhotosPreviewViewState

    private val _searchResultPhotosPreviewDataState: MutableLiveData<DataState<SearchResultPhotosPreviewViewState>> =
        MutableLiveData()
    val searchResultPhotosPreviewDataState: LiveData<DataState<SearchResultPhotosPreviewViewState>> =
        _searchResultPhotosPreviewDataState

    var PAGINATION_EXECUTING = false


    fun onEvent(searchResultPhotosPreviewStateEvents: SearchResultPhotosPreviewStateEvents) {
        when (searchResultPhotosPreviewStateEvents) {

            is SearchResultPhotosPreviewStateEvents.searchPhotos -> {
                if (searchResultPhotosPreviewStateEvents.keyword != searchResultPhotosPreviewViewState.value?.searchedKeyword) {
                    resetPagination()
                }
                setsearchedKeyword(searchResultPhotosPreviewStateEvents.keyword)
                getPhotosFromUnsplashApi(
                    searchResultPhotosPreviewStateEvents.keyword,
                    if (searchResultPhotosPreviewStateEvents.pageNo == 1) 1 else searchResultPhotosPreviewStateEvents.pageNo + 1,
                    searchResultPhotosPreviewViewState.value?.searchResultPhotos
                ).onEach {
                    _searchResultPhotosPreviewDataState.value = it
                }.launchIn(viewModelScope)
            }

            is SearchResultPhotosPreviewStateEvents.updateSortByFilter -> {
                updateSortByFilter(searchResultPhotosPreviewStateEvents.sortByFilter)
            }
            is SearchResultPhotosPreviewStateEvents.updateColorsFilter -> {
                updateColorsFilter(searchResultPhotosPreviewStateEvents.colorsList)
            }
            is SearchResultPhotosPreviewStateEvents.updateOrientationFilter -> {
                updateOrientationFilter(searchResultPhotosPreviewStateEvents.orientationFilter)
            }
            is SearchResultPhotosPreviewStateEvents.updateTagsFilter -> {
                updateTagsFilter(searchResultPhotosPreviewStateEvents.tagsList)
            }
            is SearchResultPhotosPreviewStateEvents.FilterPhotos -> {
                filterPhotos()
            }
        }
    }

    fun resetPagination() {
        _searchResultPhotosPreviewDataState.value = DataState()
        _searchResultPhotosPreviewViewState.value = SearchResultPhotosPreviewViewState()
        PAGINATION_EXECUTING = false
    }

    private fun setsearchedKeyword(keyword: String) {
        val currentViewState = getHomeViewState()
        currentViewState.searchedKeyword = keyword
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateSortByFilter(sortByFilter: SortByFilter) {
        val currentViewState = getHomeViewState()
        currentViewState.sortFilter = sortByFilter
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateOrientationFilter(orientationFilter: OrientationFilter) {
        val currentViewState = getHomeViewState()
        currentViewState.orientationFilter = orientationFilter
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateColorsFilter(list: List<String>) {
        val currentViewState = getHomeViewState()
        currentViewState.colorsFilter = list
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateTagsFilter(list: List<String>) {
        val currentViewState = getHomeViewState()
        currentViewState.tagFilter = list
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun updateCurrentPage() {
        val currentViewState = getHomeViewState()
        val currentPage = currentViewState.currentPage
        currentViewState.currentPage = currentPage + 1
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setTotalItems(count: Int) {
        val currentViewState = getHomeViewState()
        if (currentViewState.totalPhotosItems == null) currentViewState.totalPhotosItems = count
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setTotalPages(count: Int) {
        val currentViewState = getHomeViewState()
        if (currentViewState.totalPages == null) currentViewState.totalPages = count
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setSearchResultPhotosList(list: List<UnsplashPhotoInfo.photoInfo>) {
        val currentViewState = getHomeViewState()
        currentViewState.searchResultPhotos = list
        _searchResultPhotosPreviewViewState.value = currentViewState
        setDistinctTagsList()
        setDistinctColorsList()
        filterPhotos()
    }

    fun setDistinctTagsList() {
        val currentViewState = getHomeViewState()
        searchResultPhotosPreviewViewState.value?.let { viewState ->
            val tagList = viewState.distinctTagsList.toMutableList()
            viewState.searchResultPhotos?.forEach {
                it.tags_preview?.forEach {
                    if (!tagList.contains(it)) {
                        tagList.add(it)
                    }
                }
            }
            currentViewState.distinctTagsList = tagList
        }
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setDistinctColorsList() {
        val currentViewState = getHomeViewState()
        searchResultPhotosPreviewViewState.value?.let { viewState ->
            val colorList = viewState.distinctColorsList.toMutableList()
            viewState.searchResultPhotos?.forEach {
                if (!colorList.contains(it.colorCode)&&it.colorCode!="") {
                    colorList.add(it.colorCode)
                }
            }
            currentViewState.distinctColorsList = colorList
        }
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setFilteredsearchResultPhotos(list: List<UnsplashPhotoInfo.photoInfo>) {
        val currentViewState = getHomeViewState()
        currentViewState.filteredSearchResultPhotos = list
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun getHomeViewState(): SearchResultPhotosPreviewViewState {
        return _searchResultPhotosPreviewViewState.value ?: SearchResultPhotosPreviewViewState()
    }

    private fun filterPhotos() {
        searchResultPhotosPreviewViewState.value?.let { viewState ->
            filterPhotos.execute(list = viewState.searchResultPhotos ?: listOf(),
                sortByFilter = viewState.sortFilter,
                orientationFilter = viewState.orientationFilter, tagFilter = viewState.tagFilter,
                colorsFilter = viewState.colorsFilter).let {
                setFilteredsearchResultPhotos(it)
            }
        }
    }
}