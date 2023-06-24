package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state

import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo

data class SearchResultPhotosPreviewViewState(
    var searchedKeyword: String?=null,
    var searchResultPhotos: List<UnsplashPhotoInfo.photoInfo>?=null,
    var totalPages :Int?=null,
    var currentPage :Int=1,
    var totalPhotosItems:Int?=null,
    var filteredSearchResultPhotos: List<UnsplashPhotoInfo.photoInfo>?=null,
    var sortFilter:SortByFilter = SortByFilter.Relevance,
    var orientationFilter:OrientationFilter = OrientationFilter.All,
    var tagFilter :List<String> = listOf(),
    var colorsFilter :List<String> = listOf(),
    var distinctTagsList :List<String> = listOf("None"),
    var distinctColorsList :List<String> = listOf(),
)