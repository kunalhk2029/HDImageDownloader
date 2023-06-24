package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state

import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter

sealed class SearchResultPhotosPreviewStateEvents  {


    data class searchPhotos(val keyword:String,val pageNo:Int): SearchResultPhotosPreviewStateEvents()

    data class updateSortByFilter(val sortByFilter: SortByFilter):
        SearchResultPhotosPreviewStateEvents()

    data class updateOrientationFilter(val orientationFilter: OrientationFilter):
        SearchResultPhotosPreviewStateEvents()

    data class updateTagsFilter(val tagsList: List<String>): SearchResultPhotosPreviewStateEvents()

    data class updateColorsFilter(val colorsList: List<String>):
        SearchResultPhotosPreviewStateEvents()

    object FilterPhotos: SearchResultPhotosPreviewStateEvents()

}