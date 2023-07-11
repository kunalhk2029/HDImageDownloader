package com.app.imagedownloader.business.domain.Filters

sealed class OrientationFilter(val uiValue: String,val unsplashApiQueryValue:String,
val pexelsApiQueryValue: String){
    object  All: OrientationFilter("All","","all")
    object  Potrait: OrientationFilter("Portrait","portrait","portrait")
    object  Landscape: OrientationFilter("Landscape","landscape","landscape")
    object  Square: OrientationFilter("Square","squarish","square")
}
