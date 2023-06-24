package com.app.instastorytale.business.data.network.Volley.JsonObjConversion

import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.Utils.Logger
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class JsonObjConversionServiceImpl(
) : JsonObjConversionService {

    override suspend fun convertJsonObjToListOfRelatedKeywords(jsonObject: JSONObject): List<String>? {
        val list = mutableListOf<String>()

        val autoCompleteArray = jsonObject.getJSONArray("autocomplete")

        for (index in 0 until autoCompleteArray.length()) {
            val jsonObjectt = autoCompleteArray.getJSONObject(index)
            list.add(jsonObjectt.getString("query"))
        }
        return list
    }

    override suspend fun convertJsonObjToListOfUnsplashPhotoInfo(jsonObject: JSONObject): UnsplashPhotoInfo? {

        val list = mutableListOf<UnsplashPhotoInfo.photoInfo>()

        val resultArray = jsonObject.getJSONArray("results")
        val total_pages = jsonObject.getString("total_pages").toInt()
        val total = jsonObject.getString("total").toInt()

        list.add(UnsplashPhotoInfo.photoInfo(previewUrl = "previewUrl",null,
            UnsplashPhotoInfo.urls("","","","",""),false,"",null,null,0,0L))

        for (index in 0 until resultArray.length()) {
            try {
                val jsonObjectt = resultArray.getJSONObject(index)
                var description :String?=null
                try {
                    description = jsonObjectt.getString("alt_description")
                }catch (_:Exception){ }
                val color = jsonObjectt.getString("color")
                val height = jsonObjectt.getString("height").toInt()
                val width = jsonObjectt.getString("width").toInt()
                val likes = jsonObjectt.getString("likes").toInt()
                val updatedAt = jsonObjectt.getString("updated_at")
                val isPotrait = height > width
                val urlJsonObj = jsonObjectt.getJSONObject("urls")
                val userJsonObj = jsonObjectt.getJSONObject("user")
                val rawUri = urlJsonObj.getString("raw")
                val fullUri = urlJsonObj.getString("full")
                val regularUri = urlJsonObj.getString("regular")
                val smallUri = urlJsonObj.getString("small")
                val thumbUri = urlJsonObj.getString("thumb")

                val urls = UnsplashPhotoInfo.urls(
                    fullHdUrl = rawUri,
                    hdUrl = fullUri,
                    regularUrl = regularUri,
                    smallUrl = smallUri,
                    thumbnailUrl = thumbUri
                )

                var uploaderInfo: UnsplashPhotoInfo.UploaderInfo? = null
                try {
                    val username = userJsonObj.getString("username")
                    val fullname = userJsonObj.getString("name")
                    val portfolio_url = userJsonObj.getString("portfolio_url")
                    val profile_image =
                        userJsonObj.getJSONObject("profile_image").getString("medium")
                    val instagram_username = userJsonObj.getString("instagram_username")
                    uploaderInfo = UnsplashPhotoInfo.UploaderInfo(
                        creator_username = username,
                        creator_fullname = fullname, portfolio_url = portfolio_url, profile_image =
                        profile_image, instagram_username = instagram_username
                    )
                } catch (_: Exception) { }


                var tags_preview_list: MutableList<String>? = mutableListOf()
                try {
                    val tags_previewArray = jsonObjectt.getJSONArray("tags_preview")
                    for (i in 0 until tags_previewArray.length()){
                        try {
                            val tagTitle = tags_previewArray.getJSONObject(i).getString("title")
                            tags_preview_list?.add(tagTitle)
                        }catch (_:Exception){
                        }
                    }
                } catch (_: Exception) {
                    tags_preview_list=null
                }

                val serverDateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val updatedAtInEpoch = serverDateformat.parse(updatedAt.dropLast(1)).time.div(1000L)
                val unsplashPhotoInfo = UnsplashPhotoInfo.photoInfo(
                    previewUrl = regularUri,
                    uploaderInfo = uploaderInfo,
                    urls,
                    isPotrait,
                    colorCode = color,
                    description = if (description.toString()=="null") null else description,
                    tags_preview_list,
                    likes = likes,
                    updatedAt =  updatedAtInEpoch
                )
                list.add(unsplashPhotoInfo)

            } catch (_: Exception) {}
        }
        return UnsplashPhotoInfo(total_pages, totalItems = total, list)
    }
}