package com.app.imagedownloader.business.data.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo


@Entity(tableName = "fav_photos_entity")
data class FavPhotosEntity(
    @PrimaryKey(autoGenerate = false) val id:String,
    val previewUrl:String,
    var uris: UnsplashPhotoInfo.urls,
    var isPotrait: Boolean,
    var colorCode: String,
    var description: String?,
    val createdAt:Long =System.currentTimeMillis(),
){
    fun mapToFavPhotos():FavPhotos{
        return FavPhotos(
            id,previewUrl,uris, isPotrait, colorCode, description
        )
    }
}