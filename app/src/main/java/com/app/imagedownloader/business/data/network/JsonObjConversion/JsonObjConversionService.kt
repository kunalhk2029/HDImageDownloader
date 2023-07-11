package com.app.instastorytale.business.data.network.Volley.JsonObjConversion

import com.app.imagedownloader.business.data.network.dto.PinterestMediaInfo
import com.app.imagedownloader.business.data.network.dto.UnsplashPhotoInfo
import com.app.imagedownloader.business.domain.model.PexelsPhotoInfo
import com.app.imagedownloader.business.domain.model.Photo
import com.google.gson.JsonObject
import org.json.JSONObject

interface JsonObjConversionService {
    suspend fun convertJsonObjToListOfRelatedKeywords(jsonObject: JSONObject): List<String>?

    suspend fun convertJsonObjToListOfUnsplashPhotoInfo(jsonObject: JSONObject): UnsplashPhotoInfo?

    suspend fun convertJsonObjToListOfPexelsPhotoInfo(jsonObject: JSONObject): PexelsPhotoInfo?

    suspend fun convertPinterestResponseJsonObjToPinterestMediaInfo(jsonObject: JSONObject): PinterestMediaInfo?
}
