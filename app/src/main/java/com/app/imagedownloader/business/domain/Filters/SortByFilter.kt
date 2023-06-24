package com.app.imagedownloader.business.domain.Filters

sealed class SortByFilter(val uiValue: String){
    object  Relevance: SortByFilter("Relevance")
    object  Likes: SortByFilter("Likes")
    object  UploadDate: SortByFilter("Upload date")
}
