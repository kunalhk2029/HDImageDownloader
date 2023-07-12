package com.app.imagedownloader.business.domain.model

import android.net.Uri

data class DownloadedMediaInfo(
    val uri: Uri,
    val name: String,
    val created_at: Int?,
    val colorCode: String
):java.io.Serializable
