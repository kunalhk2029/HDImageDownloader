package com.app.imagedownloader.business.data.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.imagedownloader.business.domain.model.*


@Entity(tableName = "fav_photos_entity")
data class FavPhotosEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val previewUrl: String,
    var uris: Urls,
    val width: Int,
    val height: Int,
    var orienationType: String,
    var colorCode: Int,
    var description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val photoSource: String
) {
    fun mapToFavPhotos(): FavPhotos {
        return FavPhotos(
            id, previewUrl, uris, width = width, height = height, getPhotoOrienationType(orienationType), colorCode, description,
        photoSource = getPhotoSource(photoSource))
    }
}