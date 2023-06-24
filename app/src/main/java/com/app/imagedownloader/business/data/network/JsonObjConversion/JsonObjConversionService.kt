package com.app.instastorytale.business.data.network.Volley.JsonObjConversion

import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import org.json.JSONObject

interface JsonObjConversionService {
    suspend fun convertJsonObjToListOfRelatedKeywords(jsonObject: JSONObject): List<String>?

    suspend fun convertJsonObjToListOfUnsplashPhotoInfo(jsonObject: JSONObject): UnsplashPhotoInfo?
}
