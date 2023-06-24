package com.app.imagedownloader.framework.Glide

import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy

interface GlideManager {
    fun <T> setImageFromUrl(
        image: ImageView,
        url: T,
        glideSuccessUnit: (suspend () -> Unit?)? = null,
        glidefailedUnit: (suspend () -> Unit?)? = null, coilLoad: Boolean = false,
        diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    )

    fun <T> setCircularImageFromUrl(
        image: ImageView,
        url: T,
        glideSuccessUnit: (() -> Unit?)? = null,
        glidefailedUnit: (() -> Unit?)? = null,
        diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    )

    fun setImageFromUrlWithPlaceHolder(
        image: ImageView,
        url: String,
        glideSuccessUnit: (suspend () -> Unit?)? = null,
        glidefailedUnit: (suspend () -> Unit?)? = null,
        placeholder: Int,
        errorPlaceholder: Int? = null,
        diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    )

    fun getRequestManager(): RequestManager
}