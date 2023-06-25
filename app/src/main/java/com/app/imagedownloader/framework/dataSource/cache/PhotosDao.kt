package com.app.imagedownloader.framework.dataSource.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.imagedownloader.business.data.cache.model.FavPhotosEntity
import com.app.imagedownloader.business.data.cache.model.RecentSearch

@Dao
interface PhotosDao {

    @Insert
    fun insertRecentSearchQuery(recentSearch:RecentSearch):Long

    @Query("Select * from recent_searches order by createdAt desc")
    fun getRecentSearchQueries():List<RecentSearch>

    @Query("delete from recent_searches where  `query` = :recentSearchQuery")
    fun deleteRecentSearchQuery(recentSearchQuery:String):Int

    @Insert
    fun insertFavouritePhoto(favPhotosEntity: FavPhotosEntity):Long

    @Query("Select * from fav_photos_entity")
    fun getFavouritePhotos():List<FavPhotosEntity>

    @Query("Select * from fav_photos_entity where id =:id")
    fun getFavouritePhotoById(id: String):FavPhotosEntity?

    @Query("delete from fav_photos_entity where  `id` = :id")
    fun deleteFavouritePhoto(id:String):Int

}