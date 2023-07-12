package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.imagedownloader.business.data.cache.model.RecentSearch
import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.business.interactors.searchResults.FilterPhotos
import com.app.imagedownloader.business.interactors.searchResults.GetPhotos
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchResultPhotosViewModel
@Inject constructor(
    private val getPhotos: GetPhotos,
    private val filterPhotos: FilterPhotos,
    private val photosDao: PhotosDao,
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
    var scrollToAdBottom=true
    fun onEvent(searchResultPhotosPreviewStateEvents: SearchResultPhotosPreviewStateEvents) {
        when (searchResultPhotosPreviewStateEvents) {

            is SearchResultPhotosPreviewStateEvents.searchPhotos -> {
                if (searchResultPhotosPreviewStateEvents.keyword != searchResultPhotosPreviewViewState.value?.searchedKeyword) {
                    resetPagination()
                }
                setsearchedKeyword(searchResultPhotosPreviewStateEvents.keyword)
                getPhotos(searchResultPhotosPreviewStateEvents.keyword)
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

    private fun getPhotos(keyword: String) {
        getPhotos(
            keyword = keyword,
            apiSourcesInfo = searchResultPhotosPreviewViewState.value?.apiSourcesInfo
                ?: ApiSourcesInfo(),
            list = searchResultPhotosPreviewViewState.value?.searchResultPhotos,
            orientationFilter = searchResultPhotosPreviewViewState.value?.orientationFilter
                ?: OrientationFilter.All
        ).onEach {
            _searchResultPhotosPreviewDataState.value = it
        }.launchIn(viewModelScope)
    }

    fun resetPagination(orientationFilter: OrientationFilter? = null,keyword: String?=null) {
        _searchResultPhotosPreviewDataState.value = DataState()
        val newState = SearchResultPhotosPreviewViewState()
        if (orientationFilter != null) {
            _searchResultPhotosPreviewDataState.value=DataState.loading()
            newState.orientationFilter = orientationFilter
            newState.searchedKeyword = keyword
        }
        _searchResultPhotosPreviewViewState.value = newState
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
        val prevOrient = currentViewState.orientationFilter
        if (prevOrient != orientationFilter&&currentViewState.tagFilter.isNullOrEmpty()
            &&currentViewState.colorsFilter.isNullOrEmpty()
        ) {
            val searchedKeyword = currentViewState.searchedKeyword
            resetPagination(orientationFilter,searchedKeyword)
            getPhotos(searchedKeyword!!)
        }else{
            currentViewState.orientationFilterWithTagOrColorCombination=orientationFilter
            _searchResultPhotosPreviewViewState.value=currentViewState
        }
    }

    private fun updateColorsFilter(list: List<Int>) {
        val currentViewState = getHomeViewState()
        currentViewState.colorsFilter = list
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateTagsFilter(list: List<String>) {
        val currentViewState = getHomeViewState()
        currentViewState.tagFilter = list
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    private fun updateCurrentPages(apiSourcesInfo: ApiSourcesInfo) {
        val currentViewState = getHomeViewState()
        if (apiSourcesInfo.unsplashTotalPhotos != null) {
            currentViewState.apiSourcesInfo.unsplashCurrentPageNo++
        }
        if (apiSourcesInfo.pexelsTotalPhotos != null) {
            currentViewState.apiSourcesInfo.pexelsCurrentPageNo++
        }
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setApiSourcesInfo(apiSourcesInfo: ApiSourcesInfo) {
        val currentViewState = getHomeViewState()
        apiSourcesInfo.let {
            it.unsplashTotalPhotos?.let {
                Logger.log("68989898 unsplashTotalPhotos = " + it)
                currentViewState.apiSourcesInfo.unsplashTotalPhotos = it
            }
            it.unsplashPages?.let {
                Logger.log("68989898 unsplashPages = " + it)
                currentViewState.apiSourcesInfo.unsplashPages = it
            }
            it.pexelsTotalPhotos?.let {
                Logger.log("68989898 pexelsTotalPhotos = " + it)
                currentViewState.apiSourcesInfo.pexelsTotalPhotos = it
            }
            it.pexelsPages?.let {
                Logger.log("68989898 pexelsPages = " + it)
                currentViewState.apiSourcesInfo.pexelsPages = it
            }
            it.pinterestNextQueryBookMark?.let {
                Logger.log("68989898 pinterestNextQueryBookMark = " + it)
                currentViewState.apiSourcesInfo.pinterestNextQueryBookMark = it
            }
        }
        _searchResultPhotosPreviewViewState.value = currentViewState
        updateCurrentPages(apiSourcesInfo)
    }

    fun setSearchResultPhotosList(list: List<Photo>) {
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
                if (!colorList.contains(it.colorCode) && it.colorCode != 0) {
                    colorList.add(it.colorCode)
                }
            }
            currentViewState.distinctColorsList = colorList
        }
        _searchResultPhotosPreviewViewState.value = currentViewState
    }

    fun setFilteredsearchResultPhotos(list: List<Photo>) {
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
                orientationFilter = viewState.orientationFilterWithTagOrColorCombination?:viewState.orientationFilter, tagFilter = viewState.tagFilter,
                colorsFilter = viewState.colorsFilter).let {
                setFilteredsearchResultPhotos(it)
            }
        }
    }

    suspend fun getRecentSearches(): List<RecentSearch> {
        return withContext(IO) {
            photosDao.getRecentSearchQueries()
        }
    }

    suspend fun markPhotoAsFav(photoInfo: Photo): Long {
        return withContext(IO) {
            photosDao.insertFavouritePhoto(favPhotosEntity =
            photoInfo.mapToFavPhoto())
        }
    }

    suspend fun unmarkPhotoAsFav(photoInfo: Photo): Int {
        return withContext(IO) {
            photosDao.deleteFavouritePhoto(photoInfo.id)
        }
    }
}