package com.app.imagedownloader.business.interactors.searchResults

import android.graphics.Color
import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.data.network.abstraction.PhotosApiService
import com.app.imagedownloader.business.data.network.dto.AllApiData
import com.app.imagedownloader.business.data.network.dto.ApiSourcesInfo
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.NetworkBoundResource.NetworkResponseHandler
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.business.domain.model.PhotoOrienationType
import com.app.imagedownloader.business.domain.model.PhotoSource
import com.app.imagedownloader.business.domain.model.Urls
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewViewState
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPhotos @Inject constructor(private val photosApiService: PhotosApiService) {

    var job: CompletableJob = Job()

    operator fun invoke(
        keyword: String,
        apiSourcesInfo: ApiSourcesInfo,
        list: List<Photo>?,
        orientationFilter: OrientationFilter,
    ): Flow<DataState<SearchResultPhotosPreviewViewState>> {
        Logger.log("9494984 GetPhotos  =" + orientationFilter)

        return object : NetworkResponseHandler<SearchResultPhotosPreviewViewState, AllApiData?>() {
            override suspend fun doNetworkCall(): ApiResult<AllApiData?> {
                return photosApiService.getPhotos(keyword, apiSourcesInfo, orientationFilter)
            }

            override fun createSuccessDataState(apiResult: ApiResult.Success<AllApiData?>): DataState<SearchResultPhotosPreviewViewState> {
                return DataState.success(
                    apiResult.data?.let {
                        val prevList = list ?: listOf()
                        val newPhotoList: MutableList<Photo> = prevList.toMutableList()
                        newPhotoList.add(Photo(id = UUID.randomUUID().toString(), previewUrl = "previewUrl", Urls("", "", "", "", ""),
                            0, 0,  PhotoOrienationType.Potrait,0, null, null, 0, 0L, photoSource = PhotoSource.None))

                        val unsplashList = it.unsplashPhotoInfo?.photos_list?.toMutableList()
                        val pexelsList = it.pexelsPhotoInfo?.photos_list?.toMutableList()
                        val pinterestList = it.pinterestMediaInfo?.photos_list?.toMutableList()
                        while (unsplashList?.isNotEmpty() == true
                            || pexelsList?.isNotEmpty() == true
                            || pinterestList?.isNotEmpty() == true
                        ) {
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
                                it.colorCode = Color.parseColor("#00ff00")
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