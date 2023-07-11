package com.app.imagedownloader.business.data.network.abstraction

import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.dto.AllApiData
import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.domain.model.Photo

interface PhotosApiService {

    suspend fun getAutoCompleteRelatedSearchKeywords(
        url: String,
    ): ApiResult<List<String>?>

    suspend fun getPhotos(
        keyword: String,
        apiSourcesInfo: ApiSourcesInfo
    ): ApiResult<AllApiData?>

}