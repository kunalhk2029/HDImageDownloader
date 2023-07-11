package com.app.imagedownloader.business.data.cache.Utils

import androidx.room.TypeConverter
import com.app.imagedownloader.business.domain.model.Urls
import com.google.gson.Gson


class TypeConverters {

    @TypeConverter
    fun convertPhotoUrisModelIntoString(uris: Urls):String{
        return Gson().toJson(uris)
    }

    @TypeConverter
    fun convertPhotoUrisJsonStringToPhotoUrisModel(uriJsonString:String): Urls {
        return Gson().fromJson(uriJsonString, Urls::class.java)
    }
}