package com.app.imagedownloader.business.data.network.dto

import com.app.imagedownloader.business.domain.model.Photo


data class PinterestMediaInfo(
    val photos_list: List<Photo>,
    val nextQueryBookMark: String?,
) : java.io.Serializable

