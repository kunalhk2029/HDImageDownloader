package com.app.imagedownloader.business.data.network.dto

data class ApiSourcesInfo(
    var unsplashPages:Int?=null,
    var unsplashTotalPhotos:Int?=null,
    var unsplashCurrentPageNo:Int=1,

    var pexelsPages:Int?=null,
    var pexelsTotalPhotos:Int?=null,
    var pexelsCurrentPageNo:Int=1,

    var pinterestNextQueryBookMark:String?=null,

)