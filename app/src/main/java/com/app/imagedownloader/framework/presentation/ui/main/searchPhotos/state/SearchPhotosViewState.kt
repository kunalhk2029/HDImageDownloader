package com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state

data class SearchPhotosViewState(
    var finalSearchKeyword: String?=null,
    var autoCompletedRelatedSearchKeywords: List<String>?=null
)