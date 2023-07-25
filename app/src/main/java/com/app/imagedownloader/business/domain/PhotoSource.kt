package com.app.imagedownloader.business.domain


sealed class PhotoSource(val uiValue:String) :java.io.Serializable{
    object UnsplashApi:PhotoSource("unsplashApi")
    object PexelsApi:PhotoSource("pexelsApi")
    object PinterestApi:PhotoSource("pinterestApi")
    object None:PhotoSource("none")
}

fun getPhotoSource(uiValue: String): PhotoSource {
    when(uiValue){
        PhotoSource.UnsplashApi.uiValue->{
            return PhotoSource.UnsplashApi
        }
        PhotoSource.PexelsApi.uiValue->{
            return PhotoSource.PexelsApi
        }
        PhotoSource.PinterestApi.uiValue->{
            return PhotoSource.PinterestApi
        }
        else->{
            return PhotoSource.None
        }
    }
}