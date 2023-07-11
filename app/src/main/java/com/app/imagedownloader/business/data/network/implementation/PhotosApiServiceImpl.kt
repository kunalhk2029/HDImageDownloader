package com.app.imagedownloader.business.data.network.implementation

import com.android.volley.RequestQueue
import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.Volley.VolleyNetworkHandler.NetworkRequestHandler
import com.app.imagedownloader.business.data.network.abstraction.PhotosApiService
import com.app.imagedownloader.business.data.network.dto.AllApiData
import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.data.network.dto.PinterestMediaInfo
import com.app.imagedownloader.business.data.network.dto.UnsplashPhotoInfo
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.model.PexelsPhotoInfo
import com.app.imagedownloader.framework.Utils.Logger
import com.app.instastorytale.business.data.network.Volley.JsonObjConversion.JsonObjConversionService
import org.json.JSONObject

class PhotosApiServiceImpl
constructor(
    private val requestQueue: RequestQueue,
    private val jsonObjConversionService: JsonObjConversionService,
) : PhotosApiService {

    private val unsplashMap = HashMap<String, String>()
    private val pinterestMap = HashMap<String, String>()
    private val pexelsMap = HashMap<String, String>()

    init {
        unsplashMap["user-agent"] =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"
        pexelsMap["Secret-Key"] =
            "H2jk9uKnhRmL6WPwh89zBezWvr"
        pinterestMap["cookie"] =
            "_b=\"AW8nJpC7IcJDDq4QCMaWDLsJqqxJGN0ZmYo07jtiv7ubvXrkThApxncvAVeB5QWSng8=\"; csrftoken=abed6274d67d5c611f8ebc422a526e85; g_state={\"i_l\":0}; fba=True; _routing_id=\"804f0064-1d9e-4c52-b3d0-42d0ac6cd102\"; sessionFunnelEventLogged=1; cm_sub=none; _auth=0; __Secure-s_a=YS9ZVUxuZ1RQWDdYWkdKeCtWL3VvYW5OemF2akV2N1liOVlDd3NzL0FwYz0mVkdBeCtQNlZBcE1FU3hnaWladGdaUUw0eGNZPQ==; l_o=V2tFZkkvVU1Xejd5TEs0WU1sUzBmWEQ5TmVxTnFkNlpud2FDSUVWc0pNR3FXUmtJQlhPTWIwTlF0dU9ZSUFKVS9DV0tFV2ZPdndlUytINUluODVFMDBNWThHczhwUFpydk9PNGN6NFpBOHM9JmN6N1dVdjdxWE9aeE1selJ3SjRxRGN5WjJWaz0=; _pinterest_sess=TWc9PSZPNzFMaVJiTmtBMEdONjZzWHFiakpLa0hIQzRucFVBaEZvT2s4REh3K0UydmFIYXBwbW5NTitwT1NwYURlVEdZSFNZWXRBam4rcjBnTjNiblVDV3N1ZVgwcjgwZzQ1OFRRQldDa3ZqUVhhbz0mcFZyNUxmK3VSYVdPQWVnSXVrbWhHV1ZFMVZBPQ=="
        pinterestMap["X-Csrftoken"] = "abed6274d67d5c611f8ebc422a526e85"

    }

    override suspend fun getAutoCompleteRelatedSearchKeywords(url: String): ApiResult<List<String>?> {
        return object : NetworkRequestHandler<List<String>?>(url, requestQueue, unsplashMap) {
            override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): List<String>? {
                return jsonObject?.let {
                    jsonObjConversionService.convertJsonObjToListOfRelatedKeywords(
                        it
                    )
                }
            }
        }.getAsApiResult()
    }

    override suspend fun getPhotos(
        keyword: String,
        apiSourcesInfo: ApiSourcesInfo,
        orientationFilter: OrientationFilter,
    ): ApiResult<AllApiData?> {
        var unsplashPhotoInfo: UnsplashPhotoInfo? = null
        var pexelsPhotoInfo: PexelsPhotoInfo? = null
        var pinterestMediaInfo: PinterestMediaInfo? = null

        val executeUnsplashApiRequest =
            apiSourcesInfo.unsplashCurrentPageNo < (apiSourcesInfo.unsplashPages ?: 2)
        val executePexelsApiRequest =
            apiSourcesInfo.pexelsCurrentPageNo < (apiSourcesInfo.pexelsPages ?: 2)

        var allApiReachedTheirMaximumPages = true

        val unsplashUrl =
            if (orientationFilter is OrientationFilter.All)
            "https://unsplash.com/napi/search/photos?query=$keyword&per_page=20&page=${apiSourcesInfo.unsplashCurrentPageNo}&xp=search-quality-boosting%3Acontrol"
        else
        "https://unsplash.com/napi/search/photos?query=$keyword&per_page=20&page=${apiSourcesInfo.unsplashCurrentPageNo}&xp=search-quality-boosting%3Acontrol&orientation=${orientationFilter.unsplashApiQueryValue}"


        val pexelsUrl =
            "https://www.pexels.com/en-us/api/v3/search/photos?page=${apiSourcesInfo.pexelsCurrentPageNo}&per_page=24&query=$keyword&orientation=${orientationFilter.pexelsApiQueryValue}&size=all&color=all&seo_tags=true"


        val source_url = "/search/pins/?rs=ac&len=2&q=$keyword&eq=nature&etslf=5783"
        val pinterestDataFeild =
            "{\"options\":{\"article\":\"\",\"appliedProductFilters\":\"---\",\"price_max\":null,\"price_min\":null,\"query\":\"$keyword\",\"scope\":\"pins\",\"auto_correction_disabled\":\"\",\"top_pin_id\":\"\",\"filters\":\"\",\"bookmarks\":[\"${apiSourcesInfo.pinterestNextQueryBookMark}\"]},\"context\":{}}"
          val pinterestUrl =
            if (apiSourcesInfo.pinterestNextQueryBookMark != null)
                "https://in.pinterest.com/resource/BaseSearchResource/get/"
            else
                "https://in.pinterest.com/resource/BaseSearchResource/get/?source_url=%2Fsearch%2Fpins%2F%3Frs%3Dac%26len%3D2%26q%3Dmountains%26eq%mount%26etslf%3D4954&data=%7B%22options%22%3A%7B%22article%22%3A%22%22%2C%22appliedProductFilters%22%3A%22---%22%2C%22price_max%22%3Anull%2C%22price_min%22%3Anull%2C%22query%22%3A%22$keyword%22%2C%22scope%22%3A%22pins%22%2C%22auto_correction_disabled%22%3A%22%22%2C%22top_pin_id%22%3A%22%22%2C%22filters%22%3A%22%22%7D%2C%22context%22%3A%7B%7D%7D&_=1688850070807"

        Logger.log("8752877 executeUnsplashApiRequest = : " + executeUnsplashApiRequest)
        Logger.log("8752877 executePexelsApiRequest = : " + executePexelsApiRequest)

        var unsplashApiResult: ApiResult<UnsplashPhotoInfo?>? = null
        if (executeUnsplashApiRequest) {
            allApiReachedTheirMaximumPages = false
            unsplashApiResult = object :
                NetworkRequestHandler<UnsplashPhotoInfo?>(unsplashUrl, requestQueue, unsplashMap) {
                override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): UnsplashPhotoInfo? {
                    return jsonObject?.let {
                        jsonObjConversionService.convertJsonObjToListOfUnsplashPhotoInfo(
                            it
                        )
                    }
                }
            }.getAsApiResult()
        }

        var pexelsApiResult: ApiResult<PexelsPhotoInfo?>? = null
        if (executePexelsApiRequest) {
            allApiReachedTheirMaximumPages = false
            pexelsApiResult =
                object :
                    NetworkRequestHandler<PexelsPhotoInfo?>(pexelsUrl, requestQueue, pexelsMap) {
                    override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): PexelsPhotoInfo? {
                        return jsonObject?.let {
                            jsonObjConversionService.convertJsonObjToListOfPexelsPhotoInfo(
                                it
                            )
                        }
                    }
                }.getAsApiResult()
        }


//        val pinterestApiResult =
//            if (apiSourcesInfo.pinterestNextQueryBookMark != null) {
//                RetrofitInstance.api.getPinterestMedia(pinterestMap,
//                    pinterestUrl, source_url, pinterestDataFeild
//                ).let {
//                    it.let { it1 ->
//                        jsonObjConversionService.convertPinterestResponseJsonObjToPinterestMediaInfo(
//                            JSONObject(Gson().toJson(it))
//                        ).let {
//                            return@let ApiResult.Success(it)
//                        }
//                    }
//                }
//            } else {
//                object : NetworkRequestHandler<PinterestMediaInfo?>(pinterestUrl,
//                    requestQueue,
//                    pinterestMap) {
//                    override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): PinterestMediaInfo? {
//                        return jsonObject?.let {
//                            jsonObjConversionService.convertPinterestResponseJsonObjToPinterestMediaInfo(
//                                it
//                            )
//                        }
//                    }
//                }.getAsApiResult()
//            }

        var allApiFailed = true

        if (unsplashApiResult is ApiResult.Success) {
            allApiFailed = false
            unsplashApiResult.data?.let {
                unsplashPhotoInfo = it
            }
        }
        if (pexelsApiResult is ApiResult.Success) {
            allApiFailed = false
            pexelsApiResult.data?.let {
                pexelsPhotoInfo = it
            }
        }
//        if (pinterestApiResult is ApiResult.Success) {
//            allApiFailed = false
//            pinterestApiResult.data?.let {
//                pinterestMediaInfo = it
//            }
//        }
        return if (allApiFailed && !allApiReachedTheirMaximumPages)
            ApiResult.Error("") else ApiResult.Success(
            AllApiData(
                unsplashPhotoInfo, pexelsPhotoInfo, pinterestMediaInfo
            )
        )
    }


//    override suspend fun getPhotosFromPinterest(
//        url: String,
//        bookMark: String?,
//    ): ApiResult<Photo?> {
//        Logger.log("8752877 getPhotosFromPinterest = : " + bookMark)
//        return if (bookMark != null) {
//            val urll = "https://in.pinterest.com/resource/BaseSearchResource/get/"
////            val source_url = "/search/pins/?rs=ac&len=2&q=nature%20photography&eq=nature&etslf=5783"
//            val source_url = ""
//            val data =
//                "{\"options\":{\"article\":\"\",\"appliedProductFilters\":\"---\",\"price_max\":null,\"price_min\":null,\"query\":\"$url\",\"scope\":\"pins\",\"auto_correction_disabled\":\"\",\"top_pin_id\":\"\",\"filters\":\"\",\"bookmarks\":[\"$bookMark\"]},\"context\":{}}"
//            return RetrofitInstance.api.getPinterestMedia(pinterestMap,
//                urll, source_url, data
//            ).let {
//                it.let { it1 ->
//                    jsonObjConversionService.convertPinterestResponseJsonObjToListOfUnsplashPhotoInfo(
//                        it1
//                    ).let {
//                        return@let ApiResult.Success(it)
//                    }
//                }
//            }
//        } else {
//            val urll =
//                "https://in.pinterest.com/resource/BaseSearchResource/get/?source_url=%2Fsearch%2Fpins%2F%3Frs%3Dac%26len%3D2%26q%3Dmountains%26eq%mount%26etslf%3D4954&data=%7B%22options%22%3A%7B%22article%22%3A%22%22%2C%22appliedProductFilters%22%3A%22---%22%2C%22price_max%22%3Anull%2C%22price_min%22%3Anull%2C%22query%22%3A%22$url%22%2C%22scope%22%3A%22pins%22%2C%22auto_correction_disabled%22%3A%22%22%2C%22top_pin_id%22%3A%22%22%2C%22filters%22%3A%22%22%7D%2C%22context%22%3A%7B%7D%7D&_=1688850070807"
//            object : NetworkRequestHandler<Photo?>(urll, requestQueue, pinterestMap) {
//                override suspend fun doJsonObjectConversion(jsonObject: JSONObject?): Photo? {
//                    return jsonObject?.let {
//                        jsonObjConversionService.convertPinterestResponseJsonObjToListOfUnsplashPhotoInfo(
//                            it
//                        )
//                    }
//                }
//            }.getAsApiResult()
//        }
//    }
}