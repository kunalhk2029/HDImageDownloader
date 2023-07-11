package com.app.imagedownloader.business.interactors.searchResults

import android.graphics.Color
import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.abstraction.PhotosApiService
import com.app.imagedownloader.business.data.network.dto.AllApiData
import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.domain.NetworkBoundResource.NetworkResponseHandler
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewViewState
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPhotos @Inject constructor(private val photosApiService: PhotosApiService) {

    var job: CompletableJob = Job()

    operator fun invoke(
        keyword: String,
        apiSourcesInfo: ApiSourcesInfo,
        list: List<Photo>?,
    ): Flow<DataState<SearchResultPhotosPreviewViewState>> {
        return object : NetworkResponseHandler<SearchResultPhotosPreviewViewState, AllApiData?>() {
            override suspend fun doNetworkCall(): ApiResult<AllApiData?> {
                return photosApiService.getPhotos(keyword, apiSourcesInfo)
            }

            override fun createSuccessDataState(apiResult: ApiResult.Success<AllApiData?>): DataState<SearchResultPhotosPreviewViewState> {
                return DataState.success(
                    apiResult.data?.let {
                        val unsplashList =it.unsplashPhotoInfo?.photos_list?.toMutableList()
                        val pexelsList =it.pexelsPhotoInfo?.photos_list?.toMutableList()
                        val pinterestList =it.pinterestMediaInfo?.photos_list?.toMutableList()
                        val newPhotoList :MutableList<Photo> = (list ?: listOf()).toMutableList()
                        while (unsplashList?.isNotEmpty()==true
                            ||pexelsList?.isNotEmpty()==true
                            ||pinterestList?.isNotEmpty()==true){
                            val unsplashFirstPhoto = unsplashList?.firstOrNull()
                            val pexelsFirstPhoto = pexelsList?.firstOrNull()
                            val pinterestFirstPhoto = pinterestList?.firstOrNull()

                            unsplashFirstPhoto?.let {
                                unsplashList.remove(it)
                                newPhotoList.add(it)
                            }

                            pexelsFirstPhoto?.let {
                                pexelsList.remove(it)
                                newPhotoList.add(it)
                            }

                            pinterestFirstPhoto?.let {
                                pinterestList.remove(it)
                                it.colorCode=Color.parseColor("#00ff00")
                                newPhotoList.add(it)
                            }
                        }
                        SearchResultPhotosPreviewViewState(
                            searchResultPhotos = newPhotoList,
                            apiSourcesInfo = ApiSourcesInfo(
                                unsplashPages = it.unsplashPhotoInfo?.totalPages,
                                unsplashTotalPhotos = it.unsplashPhotoInfo?.totalItems,

                                pexelsPages = it.pexelsPhotoInfo?.totalPages,
                                pexelsTotalPhotos = it.pexelsPhotoInfo?.totalItems,

                                pinterestNextQueryBookMark = it.pinterestMediaInfo?.nextQueryBookMark
                            )
                        )
                    }
                )
            }
        }.result
    }
}