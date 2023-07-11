package com.app.imagedownloader.business.data.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.domain.model.PhotoSource
import com.app.imagedownloader.business.domain.model.Urls
import com.app.imagedownloader.business.domain.model.getPhotoSource


@Entity(tableName = "fav_photos_entity")
data class FavPhotosEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val previewUrl: String,
    var uris: Urls,
    val width: Int,
    val height: Int,
    var isPotrait: Boolean,
    var colorCode: Int,
    var description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val photoSource: String
) {
    fun mapToFavPhotos(): FavPhotos {
        return FavPhotos(
            id, previewUrl, uris, width = width, height = height, isPotrait, colorCode, description,
        photoSource = getPhotoSource(photoSource))
    }
}