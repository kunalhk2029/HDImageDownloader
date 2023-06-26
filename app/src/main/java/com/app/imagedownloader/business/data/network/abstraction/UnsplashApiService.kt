package com.app.imagedownloader.business.data.network.abstraction

import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo

interface UnsplashApiService {

    suspend fun getAutoCompleteRelatedSearchKeywords(
        url: String,
    ): ApiResult<List<String>?>

    suspend fun getPhotos(
        url: String,
    ): ApiResult<UnsplashPhotoInfo?>

}