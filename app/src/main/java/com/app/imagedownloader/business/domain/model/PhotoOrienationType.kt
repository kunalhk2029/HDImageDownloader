package com.app.imagedownloader.business.domain.model


sealed class PhotoOrienationType(val uiValue: String) :java.io.Serializable{
    object Potrait : PhotoOrienationType("Portrait")
    object Landscape : PhotoOrienationType("Landscape")
    object Square : PhotoOrienationType("Square")
}


fun getPhotoOrienationType(uiValue: String): PhotoOrienationType {
    when (uiValue) {
        PhotoOrienationType.Potrait.uiValue -> {
            return PhotoOrienationType.Potrait
        }
        PhotoOrienationType.Landscape.uiValue -> {
            return PhotoOrienationType.Landscape
        }
        PhotoOrienationType.Square.uiValue -> {
            return PhotoOrienationType.Square
        }
        else -> {
            return PhotoOrienationType.Potrait
        }
    }
}

fun getPhotoOrienationType(height: Int, width: Int): PhotoOrienationType {
    if (height > width) return PhotoOrienationType.Potrait
    if (height < width) return PhotoOrienationType.Landscape
    if (height == width) return PhotoOrienationType.Square
    return PhotoOrienationType.Potrait
}



