package com.app.imagedownloader.business.data.network.implementation

import com.android.volley.RequestQueue
import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.Volley.VolleyNetworkHandler.NetworkRequestHandler
import com.app.imagedownloader.business.data.network.abstraction.UnsplashApiService
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.Utils.Logger
import com.app.instastorytale.business.data.network.Volley.JsonObjConversion.JsonObjConversionService
import org.json.JSONObject

class UnsplashApiServiceImpl
constructor(
    private val requestQueue: RequestQueue,
    private val jsonObjConversionService: JsonObjConversionService
) : UnsplashApiService {

    private val map = HashMap<String, String>()

    init {
        map["user-agent"] =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"
    }

    override suspend fun getAutoCompleteRelatedSearchKeywords(url: String): ApiResult<List<String>?> {
        return object :NetworkRequestHandler<List<String>?>(url, requestQueue, map){
            override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): List<String>? {
                return jsonObject?.let {
                    jsonObjConversionService.convertJsonObjToListOfRelatedKeywords(
                        it
                    )
                }
            }
        }.getAsApiResult()
    }

    override suspend fun getPhotos(url: String): ApiResult<UnsplashPhotoInfo?> {
        return object :NetworkRequestHandler<UnsplashPhotoInfo?>(url, requestQueue, map){
            override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): UnsplashPhotoInfo? {
                return jsonObject?.let {
                    jsonObjConversionService.convertJsonObjToListOfUnsplashPhotoInfo(
                        it
                    )
                }
            }
        }.getAsApiResult()
    }
}