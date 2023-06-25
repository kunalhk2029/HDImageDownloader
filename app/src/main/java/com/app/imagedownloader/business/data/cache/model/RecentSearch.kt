package com.app.imagedownloader.business.data.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey(autoGenerate = false) val query:String,
    val createdAt:Long = System.currentTimeMillis()
)