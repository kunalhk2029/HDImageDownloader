package com.app.imagedownloader.framework.AdsManager

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
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
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class AdsManagerAdMobImpl(
    val context: Context,
) : AdsManager {

    private var nativeFullAd: NativeAd? = null
    private var nativeBannerAd: NativeAd? = null
    private var nativeHomeAd: NativeAd? = null
    private var lastFullAdLoadedTime = 0L
    private var lastHomeAdLoadedTime = 0L
    private var lastBannerAdLoadedTime = 0L

    override fun initAdsSdk() {
        MobileAds.initialize(context)
    }

    override suspend fun initNativeFullAd(): NativeAd? {
        return withContext(IO) {
            getFullAd()
        }
    }

    override suspend fun loadAdmobHomeScreenAd(
        nativeAdView: NativeAdView,
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
        return getInterstitialAd()
    }

    override fun getAdsServiceCompany(): Boolean {
        return true
    }

    override suspend fun loadAdmobNativeAd(
        admobTemplateView: TemplateView?,
        banner: Boolean,
        prevoiusBannerAd: NativeAd?,
        forceToShowPreviousAdOnly: Boolean,
        decideBannerPosition: (suspend (newAdDeteceted: Boolean) -> Unit)?,
    ): NativeAd? {
        return withContext(IO) {
            if (banner) {
                val ad = getBannerAd()
                ad.let {
                    withContext(Main) {
                        if (it != null) {
                            var newImpression = false
                            val previousAd = prevoiusBannerAd
                            if (previousAd?.body != it.body || previousAd == null
                            ) {
                                nativeBannerAd = it
                                newImpression = true
                            }
                            decideBannerPosition?.invoke(newImpression)
                        }
                    }
                    it
                }
            } else {
                val ad = if (forceToShowPreviousAdOnly) nativeFullAd else getFullAd()
                ad.let {
                    withContext(Main) {
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
                itemRootView.findViewById<CardView>(R.id.adsFreeCard).setOnClickListener {
                    CoroutineScope(IO).launch {
                        MainActivity.premiumLiveData.send(2)
                    }
                }
                if (it != null) {
                    nativeAdView.visibility = VISIBLE
                    inflateNativeAdapterItemAd(it, nativeAdView, itemRootView)
                }
            }
            it
        }
    }

    private fun inflateNativeAdapterItemAd(
        nativeAd: NativeAd,
        nativeView: NativeAdView,
        itemRootView: View,
    ) {
        val callToActionView = itemRootView.findViewById<View>(R.id.ctabttextview) as TextView
        val callToActionViewCard = itemRootView.findViewById<View>(R.id.ctaCard) as CardView
        val adSpaceholder = itemRootView.findViewById<TextView>(R.id.adspaceholder)
        adSpaceholder.visibility=View.GONE
        inflateCustomNativeAd(nativeAd,nativeView,null,callToActionView,callToActionViewCard)
    }

    private fun inflateNativeHomeAd(nativeAd: NativeAd, nativeView: NativeAdView) {
        val callToActionView = nativeView.findViewById<View>(R.id.cta) as Button
        inflateCustomNativeAd(nativeAd, nativeView, callToActionView, null,null)
    }

    private fun inflateCustomNativeAd(
        nativeAd: NativeAd,
        nativeView: NativeAdView,
        callToActionView: Button?,
        adapterItemCalltoActionView: TextView?,
        callToActionViewCard:CardView?
    ) {
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

        val customCallToActionView = callToActionView ?: adapterItemCalltoActionView
        customCallToActionView!!.visibility = VISIBLE
        ad_notification_view.visibility = VISIBLE
        var secondaryText = ""
        val iconView = nativeView.findViewById<View>(R.id.icon) as ImageView
        val mediaView = nativeView.findViewById<View>(R.id.media_view) as MediaView

        nativeAdView.callToActionView = customCallToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.mediaView = mediaView
        secondaryView.visibility = VISIBLE
        primaryView.text = headline
        customCallToActionView.text = cta
        callToActionViewCard?.visibility = VISIBLE
        if (icon != null) {
            iconView.visibility = VISIBLE
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.visibility = View.GONE
        }

        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
            if (store != null) {
                secondaryText = store
            }
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = secondaryView
            if (advertiser != null) {
                secondaryText = advertiser
            }
        } else {
            secondaryText = ""
        }
        secondaryView.text = secondaryText
        tertiaryView.text = body
        nativeAdView.bodyView = tertiaryView
        nativeAdView.setNativeAd(nativeAd)
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }

    private fun inflateNativeAd(template: TemplateView, ad: NativeAd, banner: Boolean = false) {
        val styles =
            NativeTemplateStyle.Builder()
                .build()
        template.setStyles(styles)
        template.setNativeAd(ad)
    }

    private suspend fun getBannerAd(): NativeAd? {
        if (System.currentTimeMillis() < (lastBannerAdLoadedTime + 60000L)
            && nativeBannerAd != null
        ) {
            return nativeBannerAd
        }
        lastBannerAdLoadedTime = System.currentTimeMillis()
        val ad = loadnativeAd("ca-app-pub-1622510493301918/9148314124")
        if (ad == null) lastBannerAdLoadedTime = 0L
        return ad ?: nativeBannerAd
    }

    private suspend fun getInterstitialAd(): InterstitialAd? {
        var interstitialAd: InterstitialAd? = null
        val job = Job()
        val adRequest = AdRequest.Builder().build()
        withContext(Main) {
            InterstitialAd.load(
                context,
                "ca-app-pub-1622510493301918/6781171088",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Logger.log("Debug 6565656 Ads getInterstitialAd  = " + adError.message)
                        job.complete()
                    }

                    override fun onAdLoaded(iinterstitialAd: InterstitialAd) {
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
            return nativeFullAd
        }
        lastFullAdLoadedTime = System.currentTimeMillis()
        val ad = loadnativeAd("ca-app-pub-1622510493301918/2065645427")
        if (ad != null) nativeFullAd = ad else lastFullAdLoadedTime = 0L
        return ad ?: nativeFullAd
    }

    private suspend fun getHomeAd(): NativeAd? {
        if (System.currentTimeMillis() < (lastHomeAdLoadedTime + 60000L)
            && nativeHomeAd != null
        ) {
            return nativeHomeAd
        }
        lastHomeAdLoadedTime = System.currentTimeMillis()
        val ad = loadnativeAd("ca-app-pub-1622510493301918/3752199988")
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
                    return@forNativeAd
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        ad = null
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
