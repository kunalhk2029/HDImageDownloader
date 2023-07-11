package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state

import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.Photo

data class SearchResultPhotosPreviewViewState(
    var searchedKeyword: String?=null,

    var searchResultPhotos: List<Photo>?=null,
    var filteredSearchResultPhotos: List<Photo>?=null,
    var sortFilter:SortByFilter = SortByFilter.Relevance,
    var orientationFilter:OrientationFilter = OrientationFilter.All,
    var orientationFilterWithTagOrColorCombination:OrientationFilter? = null,
    var tagFilter :List<String> = listOf(),
    var colorsFilter :List<Int> = listOf(),
    var distinctTagsList :List<String> = listOf("None"),
    var distinctColorsList :List<Int> = listOf(),

    val apiSourcesInfo: ApiSourcesInfo= ApiSourcesInfo()
)