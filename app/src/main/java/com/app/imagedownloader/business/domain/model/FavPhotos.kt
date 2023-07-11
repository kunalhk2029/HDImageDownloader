package com.app.imagedownloader.business.domain.model


data class FavPhotos(
    val id: String,
    val previewUrl: String,
    var uris: Urls,
    val width: Int,
    val height: Int,
    var isPotrait: Boolean,
    var colorCode: Int,
    var description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val photoSource: PhotoSource,
) {
    fun mapToPhoto(): Photo {
        return Photo(
            id = id,
            previewUrl,
            uris,
            width = width,
            height = height,
            isPotrait,
            colorCode, description,
            null,
            0,
            createdAt,
            true, photoSource)
    }
}