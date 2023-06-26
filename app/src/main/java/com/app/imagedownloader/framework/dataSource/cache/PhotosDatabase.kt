package com.app.imagedownloader.framework.dataSource.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.imagedownloader.business.data.cache.model.FavPhotosEntity
import com.app.imagedownloader.business.data.cache.model.RecentSearch

@Database(entities = [FavPhotosEntity::class, RecentSearch::class], version = 1, exportSchema = false)
@TypeConverters(com.app.imagedownloader.business.data.cache.Utils.TypeConverters::class)

abstract class PhotosDatabase(): RoomDatabase() {

    companion object{
        val databaseName = "Photos_Database"
    }
    abstract fun photosDao():PhotosDao
}