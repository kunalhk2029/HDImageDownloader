package com.app.imagedownloader.business.domain.Filters

sealed class OrientationFilter(val uiValue: String){
    object  All: OrientationFilter("All")
    object  Potrait: OrientationFilter("Potrait")
    object  Landscape: OrientationFilter("Landscape")
}
