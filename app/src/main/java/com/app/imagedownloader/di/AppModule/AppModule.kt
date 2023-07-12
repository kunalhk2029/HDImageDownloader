package com.app.imagedownloader.di.Hilt.GlideModule

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.Utils.PremiumFeaturesService
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPreferencesRepositoryImpl
import com.app.imagedownloader.framework.AdsManager.AdsManager
import com.app.imagedownloader.framework.AdsManager.AdsManagerAdMobImpl
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        val workManager = WorkManager.getInstance(context)
        return workManager
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val pref = context.getSharedPreferences(Constants.MAIN_SHARED_PREFS, Context.MODE_PRIVATE)
        return pref
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesRepository(sharedPreferences: SharedPreferences): SharedPrefRepository {
        return SharedPreferencesRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideAdsManager(@ApplicationContext context: Context): AdsManager {
        return AdsManagerAdMobImpl(context = context)
    }

    @Provides
    @Singleton
    fun provideGeneralAdsManager(
        adsManager: AdsManager, sharedPrefRepository: SharedPrefRepository,
        premiumFeaturesService: PremiumFeaturesService,
        @ApplicationContext context: Context
    ): GeneralAdsManager {
        return GeneralAdsManager(adsManager, context, sharedPrefRepository, premiumFeaturesService)
    }
}