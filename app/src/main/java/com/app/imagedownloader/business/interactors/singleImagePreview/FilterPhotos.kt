package com.app.imagedownloader.business.interactors.singleImagePreview

import android.graphics.Color
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.Utils.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPhotos
@Inject constructor() {

    fun execute(
        list: List<UnsplashPhotoInfo.photoInfo>, sortByFilter: SortByFilter,
        orientationFilter: OrientationFilter,
        tagFilter: List<String>,
        colorsFilter: List<String>,
    ): List<UnsplashPhotoInfo.photoInfo> {

        var filteredList = list.toMutableList()

        when (sortByFilter) {
            is SortByFilter.Relevance -> {
                filteredList = list.toMutableList()
            }
            is SortByFilter.Likes -> {
                filteredList.sortByDescending { it.likes }
            }
            is SortByFilter.UploadDate -> {
                filteredList.sortByDescending { it.updatedAt }
            }
        }

        when (orientationFilter) {
            is OrientationFilter.All -> {
                filteredList = filteredList.filter { it.isPotrait || !it.isPotrait }.toMutableList()
            }
            is OrientationFilter.Potrait -> {
                filteredList = filteredList.filter { it.isPotrait }.toMutableList()
            }
            is OrientationFilter.Landscape -> {
                filteredList = filteredList.filter { !it.isPotrait }.toMutableList()
            }
        }

        var tagFilterList: MutableList<UnsplashPhotoInfo.photoInfo> = mutableListOf()

        if (tagFilter.isEmpty()) {
            tagFilterList = filteredList
        } else {
            tagFilter.forEach { tag ->
                tagFilterList.addAll(filteredList.filter { it.tags_preview?.contains(tag) == true }
                    .toMutableList())
            }
        }

        var colorsFilterList: MutableList<UnsplashPhotoInfo.photoInfo> = mutableListOf()

        if (colorsFilter.isEmpty()) {
            colorsFilterList = tagFilterList
        } else {
            colorsFilter.forEach { color ->
                colorsFilterList.addAll(tagFilterList.filter {
                    it.colorCode!="" && Color.parseColor(it.colorCode).toString() == color
                })
            }
        }

        return colorsFilterList
    }

}