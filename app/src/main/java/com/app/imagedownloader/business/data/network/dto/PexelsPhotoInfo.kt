package com.app.imagedownloader.business.domain.model



data class PexelsPhotoInfo(
    val totalPages: Int,
    var totalItems: Int,
    val photos_list: List<Photo>,
) : java.io.Serializable

