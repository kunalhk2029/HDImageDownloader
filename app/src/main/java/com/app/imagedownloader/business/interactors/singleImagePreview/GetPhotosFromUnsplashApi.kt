package com.app.imagedownloader.business.interactors.singleImagePreview

import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.abstraction.UnsplashApiService
import com.app.imagedownloader.business.domain.DataState.DataState
import com.app.imagedownloader.business.domain.NetworkBoundResource.NetworkResponseHandler
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewViewState
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPhotosFromUnsplashApi @Inject constructor(private val unsplashApiService: UnsplashApiService) {

    var job:CompletableJob = Job()

    operator fun invoke(keyword: String, pageNo: Int,list: List<UnsplashPhotoInfo.photoInfo>?): Flow<DataState<SearchResultPhotosPreviewViewState>> {
        val url =
//            if (pageNo == null)
//                "https://unsplash.com/napi/search?query=$keyword&per_page=20&xp=search-quality-boosting%3Acontrol"
//            else "https://unsplash.com/napi/search/photos?query=$keyword&per_page=20&page=$pageNo&xp=search-quality-boosting%3Acontrol"

            "https://unsplash.com/napi/search/photos?query=$keyword&per_page=20&page=$pageNo&xp=search-quality-boosting%3Acontrol"

        return object : NetworkResponseHandler<SearchResultPhotosPreviewViewState, UnsplashPhotoInfo?>() {
            override suspend fun doNetworkCall(): ApiResult<UnsplashPhotoInfo?> {
                return unsplashApiService.getPhotos(url)
            }

            override fun createSuccessDataState(apiResult: ApiResult.Success<UnsplashPhotoInfo?>): DataState<SearchResultPhotosPreviewViewState> {
                return DataState.success(
                    apiResult.data?.let {
                        SearchResultPhotosPreviewViewState(
                            searchResultPhotos = (list?: listOf()) + it.photos_list,
                            totalPages = it.totalPages,
                            totalPhotosItems = it.totalItems
                        )
                    }
                )
            }
        }.result
    }
}