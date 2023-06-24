package com.app.imagedownloader.di.Hilt.GlideModule

import android.content.Context
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.Glide.GlideRequestManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlideModule {

    @Provides
    @Singleton
    fun provideGlideRequestManager(
        @ApplicationContext context: Context
    ): RequestManager {
        return Glide.with(context)
    }

    @Provides
    @Singleton
    fun provideGlideManager(
        requestManager: RequestManager,
    ): GlideManager {
        return GlideRequestManager(
            requestManager
        )
    }
}