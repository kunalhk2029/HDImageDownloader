package com.app.imagedownloader.business.interactors.favPhotos

import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.domain.model.PhotoOrienationType
import com.app.imagedownloader.business.domain.model.PhotoSource
import com.app.imagedownloader.business.domain.model.Urls
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
        val adList = FavPhotos("","previewUrl", uris = Urls("","","","",""), 0,0,PhotoOrienationType.Potrait,0,null, photoSource = PhotoSource.None)
        withContext(IO){
            finalList.add(adList)
            val  favPhotos = photosDao.getFavouritePhotos().sortedByDescending { it.createdAt }.map { it.mapToFavPhotos() }
            finalList.addAll(favPhotos)
            finalList.add(adList)
        }

         emit(DataState.success(FavPhotosPreviewViewState(
             finalList
         )))
    }
}