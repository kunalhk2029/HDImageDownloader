package com.app.instastorytale.business.data.network.Volley.JsonObjConversion

import android.graphics.Color
import com.app.imagedownloader.business.data.network.dto.PinterestMediaInfo
import com.app.imagedownloader.business.data.network.dto.UnsplashPhotoInfo
import com.app.imagedownloader.business.domain.model.*
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.google.gson.JsonObject
import okhttp3.internal.toHexString
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class JsonObjConversionServiceImpl(
    private val photosDao: PhotosDao,
) : JsonObjConversionService {

    override suspend fun convertJsonObjToListOfRelatedKeywords(jsonObject: JSONObject): List<String> {
        val list = mutableListOf<String>()

        val autoCompleteArray = jsonObject.getJSONArray("autocomplete")

        for (index in 0 until autoCompleteArray.length()) {
            val jsonObjectt = autoCompleteArray.getJSONObject(index)
            list.add(jsonObjectt.getString("query"))
        }
        return list
    }

    override suspend fun convertJsonObjToListOfUnsplashPhotoInfo(jsonObject: JSONObject): UnsplashPhotoInfo {

        val list = mutableListOf<Photo>()

        val resultArray = jsonObject.getJSONArray("results")
        val total_pages = jsonObject.getString("total_pages").toInt()
        val total = jsonObject.getString("total").toInt()

        for (index in 0 until resultArray.length()) {
            try {
                val jsonObjectt = resultArray.getJSONObject(index)
                val isPremiumPhoto = jsonObjectt.getBoolean("plus")
                if (isPremiumPhoto) continue
                var description: String
                description = jsonObjectt.getString("description")
                if (description == "null")
                    description = jsonObjectt.getString("alt_description")

                val id = jsonObjectt.getString("id")

                val color = jsonObjectt.getString("color")
                val height = jsonObjectt.getString("height").toInt()
                val width = jsonObjectt.getString("width").toInt()
                val likes = jsonObjectt.getString("likes").toInt()
                val updatedAt = jsonObjectt.getString("updated_at")
                val orienationType =getPhotoOrienationType(height, width)
                val urlJsonObj = jsonObjectt.getJSONObject("urls")
                val rawUri = urlJsonObj.getString("raw")
                val fullUri = urlJsonObj.getString("full")
                val regularUri = urlJsonObj.getString("regular")
                val smallUri = urlJsonObj.getString("small")
                val thumbUri = urlJsonObj.getString("thumb")

                val urls = Urls(
                    fullHdUrl = rawUri,
                    hdUrl = fullUri,
                    regularUrl = regularUri,
                    smallUrl = smallUri,
                    thumbnailUrl = thumbUri
                )

                var tags_preview_list: MutableList<String>? = mutableListOf()
                try {
                    val tags_previewArray = jsonObjectt.getJSONArray("tags_preview")
                    for (i in 0 until tags_previewArray.length()) {
                        try {
                            val tagTitle = tags_previewArray.getJSONObject(i).getString("title")
                            tags_preview_list?.add(tagTitle)
                        } catch (_: Exception) {
                        }
                    }
                } catch (_: Exception) {
                    tags_preview_list = null
                }

                val serverDateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val updatedAtInEpoch = serverDateformat.parse(updatedAt.dropLast(1)).time.div(1000L)
                val unsplashPhotoInfo = Photo(
                    id,
                    previewUrl = regularUri,
                    urls,
                    width = width,
                    height = height,
                    orienationType,
                    colorCode = Color.parseColor(color),
                    description = description,
                    tags_preview_list,
                    likes = likes,
                    updatedAt = updatedAtInEpoch,
                    isFav = photosDao.getFavouritePhotoById(id) != null,
                    photoSource = PhotoSource.UnsplashApi)
                list.add(unsplashPhotoInfo)

            } catch (_: Exception) {
            }
        }
        return UnsplashPhotoInfo(
            total_pages, totalItems = total, list
        )
    }

    override suspend fun convertJsonObjToListOfPexelsPhotoInfo(jsonObject: JSONObject): PexelsPhotoInfo? {
        val list = mutableListOf<Photo>()

        val paginationInfo = jsonObject.getJSONObject("pagination")
        val total_pages = paginationInfo.getString("total_pages").toInt()
        val total_results = paginationInfo.getString("total_results").toInt()

        val resultArray = jsonObject.getJSONArray("data")

        for (index in 0 until resultArray.length()) {
            try {
                val jsonObjectt = resultArray.getJSONObject(index).getJSONObject("attributes")
                var description: String? = jsonObjectt.getString("title")
                if (description=="null") description=null
                val id = jsonObjectt.getString("id")

                var redColorValueInInt = 0
                var greenColorValueInInt = 0
                var blueColorValueInInt = 0
                val colorsIntArray = jsonObjectt.getJSONArray("main_color")
                redColorValueInInt = colorsIntArray.getInt(0)
                greenColorValueInInt = colorsIntArray.getInt(1)
                blueColorValueInInt = colorsIntArray.getInt(2)

                val color =
                    "#${redColorValueInInt.toHexString()}${greenColorValueInInt.toHexString()}${blueColorValueInInt.toHexString()}"
                val height = jsonObjectt.getString("height").toInt()
                val width = jsonObjectt.getString("width").toInt()
                val updated_at = jsonObjectt.getString("updated_at")
                val likes = 0
                val orienationType =getPhotoOrienationType(height, width)
                val urlJsonObj = jsonObjectt.getJSONObject("image")
                val rawUri = urlJsonObj.getString("download_link")
                val fullUri = urlJsonObj.getString("download")
                val regularUri = urlJsonObj.getString("large")
                val smallUri = urlJsonObj.getString("medium")
                val thumbUri = urlJsonObj.getString("small")

                val urls = Urls(
                    fullHdUrl = rawUri,
                    hdUrl = fullUri,
                    regularUrl = regularUri,
                    smallUrl = smallUri,
                    thumbnailUrl = thumbUri
                )

                var tags_preview_list: MutableList<String>? = mutableListOf()
                try {
                    val tags_previewArray = jsonObjectt.getJSONArray("tags")
                    for (i in 0 until tags_previewArray.length()) {
                        try {
                            val tagTitle = tags_previewArray.getJSONObject(i).getString("name")
                            tags_preview_list?.add(tagTitle)
                        } catch (_: Exception) {
                        }
                    }
                } catch (_: Exception) {
                    tags_preview_list = null
                }

                val serverDateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
                val updatedAtInEpoch = serverDateformat.parse(updated_at.replace("Z","+0000")).time.div(1000L)
                val unsplashPhotoInfo = Photo(
                    id,
                    previewUrl = regularUri,
                    urls,
                    width = width,
                    height = height,
                    orienationType,
                    colorCode =
                    try {
                        Color.parseColor(color)
                    } catch (_: Exception) {
                        Color.parseColor("#121212")
                    },
                    description = description,
                    tags_preview_list,
                    likes = likes,
                    updatedAt = updatedAtInEpoch,
                    isFav = photosDao.getFavouritePhotoById(id) != null,
                    photoSource = PhotoSource.PexelsApi
                )
                list.add(unsplashPhotoInfo)

            } catch (e: Exception) {
                Logger.log("68989898 = " + e.message)
            }
        }
        return PexelsPhotoInfo(total_pages, totalItems = total_results, list)
    }

    override suspend fun convertPinterestResponseJsonObjToPinterestMediaInfo(jsonObject: JSONObject): PinterestMediaInfo? {
        val resource_response = jsonObject.getJSONObject("resource_response")
        val data = resource_response.getJSONObject("data")
        val bookmark = resource_response.getString("bookmark")
        val results = data.getJSONArray("results")
        val list = mutableListOf<Photo>()
        for (i in 0 until results.length()) {
            val currentPinItem = results.getJSONObject(i)
            currentPinItem.apply {
                val id = getString("id")
                val dominant_color = getString("dominant_color")
                val grid_title = getString("grid_title")


                val images = getJSONObject("images")
                val size170x = images.getJSONObject("170x")
//                val size236x= images.getJSONObject("236x")
//                val size474x = images.getJSONObject("474x")
//                val size736x = images.getJSONObject("736x")
//                val sizeOriginal = images.getJSONObject("orig")

                val rawUri = size170x.getString("url")
                val fullUri = size170x.getString("url")
                val regularUri = size170x.getString("url")
                val smallUri = size170x.getString("url")
                val thumbUri = size170x.getString("url")

                val width = size170x.getString("width").toInt()
                val height = size170x.getString("height").toInt()
                val orienationType =getPhotoOrienationType(height, width)

                val urls = Urls(
                    fullHdUrl = rawUri,
                    hdUrl = fullUri,
                    regularUrl = regularUri,
                    smallUrl = smallUri,
                    thumbnailUrl = thumbUri
                )
                val photo = Photo(
                    id,
                    previewUrl = regularUri,
                    urls,
                    width = width,
                    height = height,
                    orienationType,
                    colorCode = Color.parseColor(dominant_color),
                    description = grid_title,
                    null,
                    likes = 0,
                    updatedAt = 0L,
                    isFav = photosDao.getFavouritePhotoById(id) != null,
                    photoSource = PhotoSource.PinterestApi)
                list.add(photo)
            }
        }
        Logger.log("8752877 convertPinterestResponseJsonObjToListOfUnsplashPhotoInfo = : " + bookmark)
        Logger.log("8752877 convertPinterestResponseJsonObjToListOfUnsplashPhotoInfo = : " + list.size)

        return PinterestMediaInfo(list, bookmark)
    }
}