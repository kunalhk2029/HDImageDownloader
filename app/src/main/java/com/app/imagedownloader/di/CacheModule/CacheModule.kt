package com.app.imagedownloader.di.CacheModule

import android.content.Context
import androidx.room.Room
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.app.imagedownloader.framework.dataSource.cache.PhotosDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class CacheModule {

    @Singleton
    @Provides
    fun providePhotosDatabse(@ApplicationContext context: Context):PhotosDatabase{
      return  Room.databaseBuilder(context,PhotosDatabase::class.java,PhotosDatabase.databaseName)
            .build()
    }

    @Singleton
    @Provides
    fun providePhotosDao(photosDatabase: PhotosDatabase):PhotosDao{
        return photosDatabase.photosDao()
    }
}