package com.app.imagedownloader.framework.AdsManager

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import dagger.hilt.internal.aggregatedroot.AggregatedRoot
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class AdsManagerAdMobImpl(
    val context: Context
) : AdsManager {

    private var nativeFullAd: NativeAd? = null
    private var nativeBannerAd: NativeAd? = null
    private var nativeHomeAd: NativeAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialAdLoadedTime = 0L
    private var lastFullAdLoadedTime = 0L
    private var lastHomeAdLoadedTime = 0L
    private var lastBannerAdLoadedTime = 0L

    override fun initAdsSdk() {
        MobileAds.initialize(context)
        initNativeAdsPreload()
    }

    override suspend fun loadAdmobHomeScreenAd(
        nativeAdView: NativeAdView
    ) {
        val ad = getHomeAd() ?: nativeHomeAd
        ad.let {
            withContext(Main) {
                if (it != null) {
                    nativeAdView.visibility = View.VISIBLE
                    inflateNativeHomeAd(it, nativeAdView)
                }
            }
            it
        }
    }

    override fun getpreLoadedAd(banner: Boolean): NativeAd? {
        return if (banner) nativeBannerAd else nativeFullAd
    }


    override suspend fun getpreLoadedInterstitialAd(skipTimeLimit: Boolean): InterstitialAd? {
        if (skipTimeLimit) return getInterstitialAd(true)
        if (!skipTimeLimit&&interstitialAd==null) return getInterstitialAd()
        return interstitialAd
    }

    override fun initNativeAdsPreload() {
        CoroutineScope(IO).launch {
            launch {
                getFullAd()
            }
            launch {
                getInterstitialAd()
            }
        }
    }

    override fun getAdsServiceCompany(): Boolean {
        return true
    }

    override suspend fun loadAdmobNativeAd(
        admobTemplateView: TemplateView?,
        banner: Boolean,
        prevoiusBannerAd: NativeAd?,
        decideBannerPosition: (suspend (newAdDeteceted: Boolean) -> TemplateView?)?
    ): NativeAd? {
        return withContext(IO) {
            if (banner) {
                val ad = getBannerAd() ?: nativeBannerAd
                Logger.log("Debug 64656 = " + ad?.body)
                Logger.log("Debug 64656 1 = " + prevoiusBannerAd?.body)
                ad.let {
                    withContext(Dispatchers.Main) {
                        if (it != null) {
                            var newImpression = false
                            val previousAd = prevoiusBannerAd
                            if (previousAd?.body != it.body || previousAd == null
                            ) {
                                nativeBannerAd = it
                                newImpression = true
                            }
                            decideBannerPosition?.invoke(newImpression)?.let { templateView ->
                                Logger.log("Debug 64656 2 = " + templateView)
                                inflateNativeAd(templateView, it, true)
                            }
                        }
                    }
                    it
                }
            } else {
                val ad = nativeFullAd ?: getFullAd()
                ad.let {
                    withContext(Dispatchers.Main) {
                        if (it != null) {
                            if (admobTemplateView != null) {
                                inflateNativeAd(admobTemplateView, it)
                            }
                        }
                    }
                    it
                }
            }
        }
    }

    override suspend fun loadAdmobAdapterItemAd(nativeAdView: NativeAdView, itemRootView: View) {
        val ad = getHomeAd() ?: nativeHomeAd
        ad.let {
            withContext(Main) {
                if (it != null) {
                    nativeAdView.visibility = View.VISIBLE
                    inflateNativeAdapterItemAd(it, nativeAdView,itemRootView)
                }
            }
            it
        }
    }

    private fun inflateNativeAdapterItemAd(nativeAd: NativeAd, nativeView: NativeAdView, itemRootView: View) {
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val icon = nativeAd.icon
        val nativeAdView = nativeView.findViewById<View>(R.id.native_ad_view) as NativeAdView
        val ad_notification_view =
            nativeView.findViewById<View>(R.id.ad_notification_view) as TextView
        val primaryView = nativeView.findViewById<View>(R.id.primary) as TextView
        val secondaryView = nativeView.findViewById<View>(R.id.secondary) as TextView
        val tertiaryView = nativeView.findViewById<View>(R.id.body) as TextView

        val callToActionView = itemRootView.findViewById<View>(R.id.ctabttextview) as TextView
        val callToActionViewCard = itemRootView.findViewById<View>(R.id.ctaCard) as CardView
        callToActionView.visibility = VISIBLE
        ad_notification_view.visibility = VISIBLE
        callToActionViewCard.visibility = VISIBLE


        val iconView = nativeView.findViewById<View>(R.id.icon) as ImageView
        val mediaView = nativeView.findViewById<View>(R.id.media_view) as MediaView

        nativeAdView.setCallToActionView(callToActionView)
        nativeAdView.setHeadlineView(primaryView)
        nativeAdView.setMediaView(mediaView)
        secondaryView.setVisibility(View.VISIBLE)
        primaryView.setText(headline)
        callToActionView.setText(cta)

        if (icon != null) {
            iconView.setVisibility(View.VISIBLE)
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.setVisibility(View.GONE)
        }
        tertiaryView.setText(body)
        nativeAdView.setBodyView(tertiaryView)
        nativeAdView.setNativeAd(nativeAd)
    }

    private fun inflateNativeHomeAd(nativeAd: NativeAd, nativeView: NativeAdView) {
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val icon = nativeAd.icon
        val nativeAdView = nativeView.findViewById<View>(R.id.native_ad_view) as NativeAdView
        val ad_notification_view =
            nativeView.findViewById<View>(R.id.ad_notification_view) as TextView
        val primaryView = nativeView.findViewById<View>(R.id.primary) as TextView
        val secondaryView = nativeView.findViewById<View>(R.id.secondary) as TextView
        val tertiaryView = nativeView.findViewById<View>(R.id.body) as TextView

        val callToActionView = nativeView.findViewById<View>(R.id.cta) as Button
        callToActionView.visibility = VISIBLE
        ad_notification_view.visibility = VISIBLE

        val iconView = nativeView.findViewById<View>(R.id.icon) as ImageView
        val mediaView = nativeView.findViewById<View>(R.id.media_view) as MediaView

        nativeAdView.setCallToActionView(callToActionView)
        nativeAdView.setHeadlineView(primaryView)
        nativeAdView.setMediaView(mediaView)
        secondaryView.setVisibility(View.VISIBLE)
        primaryView.setText(headline)
        callToActionView.setText(cta)

        if (icon != null) {
            iconView.setVisibility(View.VISIBLE)
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.setVisibility(View.GONE)
        }
        tertiaryView.setText(body)
        nativeAdView.setBodyView(tertiaryView)
        nativeAdView.setNativeAd(nativeAd)
    }

    private fun inflateNativeAd(template: TemplateView, ad: NativeAd, banner: Boolean = false) {
        val styles =
            NativeTemplateStyle.Builder()
                .build()
        template.setStyles(styles)
        template.setNativeAd(ad)
        if (banner) {
//            template.premiumButton.setOnClickListener {
//                CoroutineScope(IO).launch {
//                    MainActivity.premiumLiveData.send(2)
//                }
//            }
        }
    }

    private suspend fun getBannerAd(): NativeAd? {
        if (System.currentTimeMillis() < (lastBannerAdLoadedTime + 60000L)
            && nativeBannerAd != null
        ) {
            Logger.log("Debug admob Ads getBannerAd   time less than 1 Min........")
            return nativeBannerAd
        }
        lastBannerAdLoadedTime = System.currentTimeMillis()
        Logger.log("Debug admob Ads getBannerAd time more // than 1 Min........")
        val ad = loadnativeAd("ca-app-pub-1622510493301918/3289764003")
        if (ad == null) lastBannerAdLoadedTime = 0L
        return ad ?: nativeBannerAd
    }

    private suspend fun getInterstitialAd(skipTimeLimit: Boolean = false): InterstitialAd? {
        if ((System.currentTimeMillis() < (lastInterstitialAdLoadedTime + 30000L)
                    && interstitialAd != null) && !skipTimeLimit
        ) {
            Logger.log("Debug 6565656 Ads getInterstitialAd   time less than 2 Min........")
            Logger.log("Debug admob Ads getInterstitialAd   time less than 2 Min........")
            return interstitialAd
        }
        interstitialAd = null
        lastInterstitialAdLoadedTime = System.currentTimeMillis()
        Logger.log("Debug 6565656 Ads getInterstitialAd time more // than 2 Min........")
        Logger.log("Debug admob Ads getInterstitialAd time more // than 2 Min........")

        val job = Job()
        val adRequest = AdRequest.Builder().build()
        withContext(Main) {
            InterstitialAd.load(
                context,
                "ca-app-pub-1622510493301918/1914524348",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Logger.log("Debug 6565656 Ads getInterstitialAd  = " + adError.message)
                        lastInterstitialAdLoadedTime = 0L
                        job.complete()
                    }

                    override fun onAdLoaded(iinterstitialAd: InterstitialAd) {
                        Logger.log("Debug 6565656 Ads getInterstitialAd  = " + iinterstitialAd)
                        interstitialAd = iinterstitialAd
                        job.complete()
                    }
                })
        }
        while (!job.isCompleted) {
            delay(1000L)
        }
        return interstitialAd
    }

    private suspend fun getFullAd(): NativeAd? {
        if (System.currentTimeMillis() < (lastFullAdLoadedTime + 60000L)
            && nativeFullAd != null
        ) {
            Logger.log("Debug 6565656 Ads getFullAd   time less than 2 Min........")
            Logger.log("Debug admob Ads getFullAd   time less than 2 Min........")
            return nativeFullAd
        }
        lastFullAdLoadedTime = System.currentTimeMillis()
        Logger.log("Debug 6565656 Ads getFullAd time more // than 2 Min........")
        Logger.log("Debug admob Ads getFullAd time more // than 2 Min........")
        val ad = loadnativeAd("ca-app-pub-1622510493301918/7233326529")
        if (ad != null) nativeFullAd = ad else lastFullAdLoadedTime = 0L
        return ad ?: nativeFullAd
    }

    private suspend fun getHomeAd(): NativeAd? {
        if (System.currentTimeMillis() < (lastHomeAdLoadedTime + 60000L)
            && nativeHomeAd != null
        ) {
            Logger.log("Debug admob Ads getHomeAd   time less than 1 Min........")
            return nativeHomeAd
        }
        lastHomeAdLoadedTime = System.currentTimeMillis()
        Logger.log("Debug admob Ads getHomeAd time more // than 1 Min........")
        val ad = loadnativeAd("ca-app-pub-1622510493301918/5920244855")
        if (ad != null) nativeHomeAd = ad else lastHomeAdLoadedTime = 0L
        return ad ?: nativeHomeAd
    }

    private suspend fun loadnativeAd(adUnitId: String): NativeAd? {
        val job = Job()
        var ad: NativeAd? = null
        val adLoader: AdLoader =
            AdLoader.Builder(context, adUnitId)
                .forNativeAd { p0 ->
                    ad = p0
                    job.complete()
                    Logger.log("Debug ADMob  native loaded")
                    return@forNativeAd
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        ad = null
                        Logger.log("Debug admob AdFailed  = " + p0.message)
                        job.complete()
                    }
                }).build()
        adLoader.loadAd(AdRequest.Builder().build())

        while (!job.isCompleted) {
            delay(1000L)
        }
        if (nativeBannerAd == null) nativeBannerAd = ad
        if (nativeFullAd == null) nativeFullAd = ad
        if (nativeHomeAd == null) nativeHomeAd = ad
        return ad
    }
}
