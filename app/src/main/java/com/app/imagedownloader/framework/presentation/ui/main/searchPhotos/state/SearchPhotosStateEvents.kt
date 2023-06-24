package com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state

sealed class SearchPhotosStateEvents {

    class autoCompleteSearchKeywordClicked(val keyword: String) : SearchPhotosStateEvents()

    class searchInitiated(val keyword: String) : SearchPhotosStateEvents()

    class searchBarTextChanged(val keyword: String) : SearchPhotosStateEvents()
}