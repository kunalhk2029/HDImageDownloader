package com.app.imagedownloader.framework.Glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class GlideRequestManager(
    private val requestManager: RequestManager,
) : GlideManager {


    override fun <T> setImageFromUrl(
        image: ImageView,
        url: T,
        glideSuccessUnit: (suspend () -> Unit?)?,
        glidefailedUnit: (suspend () -> Unit?)?,
        coilLoad: Boolean,
        diskCacheStrategy: DiskCacheStrategy
    ) {
        requestManager.load(url).diskCacheStrategy(diskCacheStrategy)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glidefailedUnit?.invoke()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glideSuccessUnit?.invoke()
                    }
                    return false
                }
            }).into(image)
    }

    override fun <T> setCircularImageFromUrl(
        image: ImageView,
        url: T,
        glideSuccessUnit: (() -> Unit?)?,
        glidefailedUnit: (() -> Unit?)?,
        diskCacheStrategy: DiskCacheStrategy
    ) {
        requestManager.load(url).diskCacheStrategy(diskCacheStrategy)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glidefailedUnit?.invoke()
                    }

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glideSuccessUnit?.invoke()
                    }
                    return false
                }
            })
            .circleCrop().into(image)
    }


    override fun setImageFromUrlWithPlaceHolder(
        image: ImageView,
        url: String,
        glideSuccessUnit: (suspend () -> Unit?)?,
        glidefailedUnit: (suspend () -> Unit?)?,
        placeholder: Int,
        errorPlaceholder: Int?, diskCacheStrategy: DiskCacheStrategy
    ) {
        val requestOptions: RequestOptions =
            errorPlaceholder?.let { RequestOptions().placeholder(placeholder).error(it) }
                ?: kotlin.run {
                    RequestOptions().placeholder(placeholder)
                }
        requestManager.load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glidefailedUnit?.invoke()
                    }

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Main).launch {
                        glideSuccessUnit?.invoke()
                    }
                    return false
                }
            })
            .diskCacheStrategy(diskCacheStrategy).apply(requestOptions)
            .into(image)
    }
    override fun getRequestManager(): RequestManager {
        return requestManager
    }
}