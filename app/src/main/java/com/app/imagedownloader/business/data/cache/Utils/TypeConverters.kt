package com.app.imagedownloader.business.data.cache.Utils

import androidx.room.TypeConverter
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.google.gson.Gson


class TypeConverters {

    @TypeConverter
    fun convertPhotoUrisModelIntoString(uris: UnsplashPhotoInfo.urls):String{
        return Gson().toJson(uris)
    }

    @TypeConverter
    fun convertPhotoUrisJsonStringToPhotoUrisModel(uriJsonString:String):UnsplashPhotoInfo.urls{
        return Gson().fromJson(uriJsonString,UnsplashPhotoInfo.urls::class.java)
    }
}