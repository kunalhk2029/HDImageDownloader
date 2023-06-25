package com.app.imagedownloader.business.domain.model


data class FavPhotos(
    val id:String,
    val previewUrl:String,
    var uris: UnsplashPhotoInfo.urls,
    var isPotrait: Boolean,
    var colorCode: String,
    var description: String?,
    val createdAt:Long =System.currentTimeMillis(),
)