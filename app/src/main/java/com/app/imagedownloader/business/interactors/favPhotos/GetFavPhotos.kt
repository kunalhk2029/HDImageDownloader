package com.app.imagedownloader.business.interactors.favPhotos

import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview.state.FavPhotosPreviewViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFavPhotos
@Inject constructor(private val photosDao: PhotosDao) {
     fun execute():Flow<DataState<FavPhotosPreviewViewState>> = flow {
        emit(DataState.loading())

        val finalList = mutableListOf<FavPhotos>()
        val adList = FavPhotos("","previewUrl", uris = UnsplashPhotoInfo.urls("","","","",""),
        false,"",null)
        withContext(IO){
            finalList.add(adList)
            val  favPhotos = photosDao.getFavouritePhotos().sortedByDescending { it.createdAt }.map { it.mapToFavPhotos() }
            finalList.addAll(favPhotos)
        }
         emit(DataState.success(FavPhotosPreviewViewState(
             finalList
         )))
    }
}