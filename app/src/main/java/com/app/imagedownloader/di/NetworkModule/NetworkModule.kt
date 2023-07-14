package com.app.imagedownloader.di.Hilt.GlideModule

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.app.imagedownloader.business.data.network.abstraction.PhotosApiService
import com.app.imagedownloader.business.data.network.implementation.PhotosApiServiceImpl
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.app.imagedownloader.business.data.network.Volley.JsonObjConversion.JsonObjConversionService
import com.app.imagedownloader.business.data.network.Volley.JsonObjConversion.JsonObjConversionServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePinterestMediaService(
        requestQueue: RequestQueue,jsonObjConversionService: JsonObjConversionService
    ): PhotosApiService {
        return PhotosApiServiceImpl(requestQueue,jsonObjConversionService)
    }

    @Provides
    @Singleton
    fun provideRequestQueue(
        @ApplicationContext context: Context
    ): RequestQueue {
        return Volley.newRequestQueue(context)
    }

    @Provides
    @Singleton
    fun provideJsonObjConversionService(
        photosDao: PhotosDao
    ): JsonObjConversionService {
        return JsonObjConversionServiceImpl(photosDao)
    }
}