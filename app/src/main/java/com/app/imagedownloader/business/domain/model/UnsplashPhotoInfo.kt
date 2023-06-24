package com.app.imagedownloader.business.domain.model



data class UnsplashPhotoInfo(
    val totalPages :Int,
    var totalItems:Int,
    val photos_list: List<photoInfo>
):java.io.Serializable{

    data class photoInfo(
        val previewUrl: String,
        val uploaderInfo: UploaderInfo?,
        var uris: urls,
        var isPotrait: Boolean,
        var colorCode: String,
        var description: String?,
        val tags_preview: List<String>?,
        val likes:Int,
        val updatedAt:Long,
    ):java.io.Serializable
    data class urls(
        val fullHdUrl: String,
        val hdUrl: String,
        val regularUrl: String,
        val smallUrl: String,
        val thumbnailUrl: String
    ):java.io.Serializable

    data class UploaderInfo(
        val creator_username: String,
        val creator_fullname: String,
        val portfolio_url: String?,
        val profile_image: String,
        val instagram_username: String,
    ):java.io.Serializable
}

