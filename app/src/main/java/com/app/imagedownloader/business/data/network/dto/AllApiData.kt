package com.app.imagedownloader.business.data.network.dto

import com.app.imagedownloader.business.domain.model.PexelsPhotoInfo

data class AllApiData(
    val unsplashPhotoInfo: UnsplashPhotoInfo?,
    val pexelsPhotoInfo: PexelsPhotoInfo?,
    val pinterestMediaInfo: PinterestMediaInfo?
)