package com.app.imagedownloader.business.data.network.dto

import com.app.imagedownloader.business.domain.model.Photo


data class UnsplashPhotoInfo(
    val totalPages: Int,
    var totalItems: Int,
    val photos_list: List<Photo>,
) : java.io.Serializable



