package com.app.imagedownloader.framework.AdsManager

import android.service.media.MediaBrowserService.BrowserRoot
import android.view.View
import androidx.cardview.widget.CardView
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

interface AdsManager {

    fun initAdsSdk()

    fun getAdsServiceCompany(): Boolean

    fun getpreLoadedAd(banner: Boolean): NativeAd?

    suspend fun getpreLoadedInterstitialAd(skipTimeLimit: Boolean = false): InterstitialAd?

    fun initNativeAdsPreload()

    suspend fun loadAdmobHomeScreenAd(
        nativeAdView: NativeAdView
    ) {
    }

    suspend fun loadAdmobAdapterItemAd(
        nativeAdView: NativeAdView,itemRootView:View
    ) {
    }
    suspend fun loadAdmobNativeAd(
        admobTemplateView: TemplateView?, banner: Boolean,
        prevoiusBannerAd: NativeAd? = null,
        decideBannerPosition: (suspend (newAdDeteceted: Boolean) -> TemplateView?)? = null
    ): NativeAd? {
        return null
    }
}