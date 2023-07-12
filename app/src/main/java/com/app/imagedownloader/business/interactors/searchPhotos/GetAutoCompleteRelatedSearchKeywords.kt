package com.app.imagedownloader.business.interactors.searchPhotos

import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.abstraction.PhotosApiService
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.NetworkBoundResource.NetworkResponseHandler
import com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state.SearchPhotosViewState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAutoCompleteRelatedSearchKeywords
@Inject constructor(private val photosApiService: PhotosApiService) {
    operator fun invoke(keyword: String): Flow<DataState<SearchPhotosViewState>> {
        val url = "https://unsplash.com/nautocomplete/$keyword?xp=search-quality-boosting%3Acontrol"
        return object : NetworkResponseHandler<SearchPhotosViewState, List<String>?>() {
            override suspend fun doNetworkCall(): ApiResult<List<String>?> {
                return photosApiService.getAutoCompleteRelatedSearchKeywords(url)
            }
            override fun createSuccessDataState(apiResult: ApiResult.Success<List<String>?>): DataState<SearchPhotosViewState> {
                return DataState.success(SearchPhotosViewState(autoCompletedRelatedSearchKeywords = apiResult.data))
            }
        }.result
    }
}