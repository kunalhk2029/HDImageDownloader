package com.app.imagedownloader.business.domain.model

import com.app.imagedownloader.business.data.cache.model.FavPhotosEntity

data class Photo(
    val id: String,
    val previewUrl: String,
    var urls: Urls,
    var width: Int,
    var height: Int,
    var isPotrait: Boolean,
    var colorCode: Int,
    var description: String?,
    val tags_preview: List<String>?,
    val likes: Int,
    val updatedAt: Long,
    var isFav: Boolean = false,
    var photoSource: PhotoSource,
) : java.io.Serializable{

    fun mapToFavPhoto(): FavPhotosEntity {
       return FavPhotosEntity(id,
            previewUrl,
            urls,
            width=width,
            height=height,
            isPotrait,
            colorCode,
            description, photoSource = photoSource.uiValue)
    }
}

data class Urls(
    val fullHdUrl: String,
    val hdUrl: String,
    val regularUrl: String,
    val smallUrl: String,
    val thumbnailUrl: String,
) : java.io.Serializable
