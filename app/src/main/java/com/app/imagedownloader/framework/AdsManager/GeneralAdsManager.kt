package com.app.imagedownloader.framework.AdsManager

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.PremiumFeaturesService
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.app.instastorytale.framework.presentation.Adapters.ViewPagerAdapter2
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


class GeneralAdsManager(
    val adsManager: AdsManager,
    private val sharedPrefRepository: SharedPrefRepository,
    private val premiumFeaturesService: PremiumFeaturesService,
) {


    init {
        adsManager.initAdsSdk()
    }

    companion object {
        var bannerAdVisibilityHidden = false
        var adsPremiumPlanPurchased: Boolean? = null
        val bannerAdPlaceHolderList: HashMap<Int, BannerAdPlaceholder> = HashMap()
        val bannerAdList: HashMap<Int, NativeAd> = HashMap()
    }

    var bannerAdPosition = 0
    var mediator: TabLayoutMediator? = null

    suspend fun isAdsFreePlanPurchased(): Boolean? {
        if (adsPremiumPlanPurchased != null) return adsPremiumPlanPurchased
        adsPremiumPlanPurchased = premiumFeaturesService.isAdsFreePlanPurchased()
        return adsPremiumPlanPurchased
    }

    private fun showNativeAd(
        view: View,
        job: CompletableJob, activity: MainActivity, id: Int, showToolbar: Boolean,
        instantlyshowNativeInterstitialAdProgressBar: Boolean,
    ) {
        if (adsPremiumPlanPurchased == true || adsPremiumPlanPurchased == null) {
            job.complete()
            return
        }
        val adloadingob = view.findViewById<ProgressBar>(R.id.adprogressbar)
        val adcover = view.findViewById<LinearLayout>(R.id.adloadingCover)
        val addl = view.findViewById<ConstraintLayout>(R.id.addl)
        val adl: ConstraintLayout = view as ConstraintLayout
        CoroutineScope(Main).launch {
            val adcounter = view.findViewById<TextView>(R.id.adcounter)
            val adpb = view.findViewById<ProgressBar>(R.id.adpb)
            val adclose = view.findViewById<ImageView>(R.id.closead)
            adclose.visibility = View.INVISIBLE
            adcounter.visibility = View.VISIBLE
            adpb.visibility = View.VISIBLE
            if (adsManager.getAdsServiceCompany()) {
                val template = view.findViewById<TemplateView>(R.id.my_template)
                val adMobNativeAd =
                    adsManager.loadAdmobNativeAd(
                        template,
                        false,
                        null,
                        forceToShowPreviousAdOnly = instantlyshowNativeInterstitialAdProgressBar,
                        null
                    )
                adcover.visibility = View.GONE
                adloadingob.visibility = View.GONE
                adMobNativeAd?.let { nativeAd ->
                    addl.visibility = View.GONE
                    adl.visibility = View.VISIBLE
                    handleNativeFullUi(
                        template,
                        view,
                        id,
                        activity,
                        job, showToolbar
                    )
                } ?: kotlin.run {
                    Logger.log("68989898 2......")
                    adl.visibility = View.GONE
                    job.complete()
                }
            }
        }
    }

    private fun handleNativeFullUi(
        template: TemplateView?,
        view: View,
        id: Int,
        activity: MainActivity,
        job: CompletableJob, showToolbar: Boolean,
    ) {
        val adl = view
        val addl = view.findViewById<ConstraintLayout>(R.id.addl)
        val adcounter = view.findViewById<TextView>(R.id.adcounter)
        val adpb = view.findViewById<ProgressBar>(R.id.adpb)
        val adclose = view.findViewById<ImageView>(R.id.closead)
        addl.visibility = View.VISIBLE
        adpb.max = 100
        adpb.progress = -20

        activity.hideToolbar()
        adl.visibility = View.VISIBLE
        template?.visibility = View.VISIBLE
        adclose.setOnClickListener {
            if (showToolbar) activity.showToolbar()
            if (currentFragId == id) {
                activity.binding?.badd?.root?.visibility = View.VISIBLE
            }
            adl.visibility = View.GONE
            job.complete()
        }
        val timer = object : CountDownTimer(5000L, 1000L) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                adcounter.text = (p0.div(1000) + 1).toString()
                adpb.progress = adpb.progress + 20
            }

            override fun onFinish() {
                adcounter.visibility = View.GONE
                adpb.visibility = View.GONE
                adpb.progress = -20
                adclose.visibility = View.VISIBLE
            }
        }
        timer.start()
    }


    var currentFragId = -1
    var activityPausedByInterstitialAd = false

    private suspend fun showInterstitialAd(
        executeFun: () -> Unit,
        activity: MainActivity,
        id: Int,
        showToolbar: Boolean = true,
    ) {

        Logger.log("9595555595 .........")
        val interstitialAd = adsManager.getpreLoadedInterstitialAd()

        interstitialAd?.fullScreenContentCallback = object :
            FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                Logger.log("55666595 ............  // //  showFullScreenInterstitialAd = error  " + p0.message)
                CoroutineScope(Main).launch {
                    showNativeFullAd(executeFun, activity, id, showToolbar, false)
                }
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                executeFun.invoke()
            }
        }
        interstitialAd?.let {
            activityPausedByInterstitialAd = true
            it.show(activity)
        } ?: kotlin.run {
            executeFun.invoke()
        }
    }

    suspend fun showNativeAdapterItemAd(
        root: View,
    ): Boolean {

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return true
        }
        if (adsPremiumPlanPurchased == null) {
            isAdsFreePlanPurchased()
        }
        val adView = root.findViewById(R.id.native_ad_view) as NativeAdView
        adsPremiumPlanPurchased?.let {
            if (it) {
                adView.visibility = View.GONE
                return@let
            }
            adView.visibility = View.VISIBLE
            adsManager.loadAdmobAdapterItemAd(adView, itemRootView = root)
        } ?: kotlin.run {
            adView.visibility = View.GONE
        }
        return adsPremiumPlanPurchased ?: true
    }

    suspend fun showNativeHomeScreenAd(
        view: NativeAdView,
    ) {

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return
        }
        if (adsPremiumPlanPurchased == null) {
            isAdsFreePlanPurchased()
        }
        adsPremiumPlanPurchased?.let {
            if (it) {
                view.visibility = View.GONE
                return@let
            }
            adsManager.loadAdmobHomeScreenAd(view)
        } ?: kotlin.run {
            view.visibility = View.GONE
        }
    }

    suspend fun showNativeBannerAd(
        view: View,
        a: AppBarLayout? = null, adSpace: ConstraintLayout,
        manager: FragmentManager,
        lifecycle: Lifecycle,
    ) {

        Logger.log("9559595 showNativeBannerAd invoked ")

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return
        }

        if (adsPremiumPlanPurchased == null) {
            isAdsFreePlanPurchased()
        }
        adsPremiumPlanPurchased?.let {
            if (it) {
                adSpace.visibility = View.GONE
                view.visibility = View.GONE
                return@let
            }

            if (bannerAdVisibilityHidden) return@let

            val adsfreebt = adSpace.findViewById<CardView>(R.id.adsfreebt)
            adsfreebt.visibility = View.GONE

            CoroutineScope(Main).launch {
                val viewPager2 = view.findViewById<ViewPager2>(R.id.bannerAdViewPager)
                val tabLayout = view.findViewById<TabLayout>(R.id.tablayout)
                if (viewPager2.adapter == null) {
                    adSpace.visibility = View.VISIBLE
                    view.visibility = View.GONE
                }
                if (adsManager.getAdsServiceCompany()) {
                    val previousAd: NativeAd? =
                        if (adsManager.getpreLoadedAd(true)?.callToAction != null) adsManager.getpreLoadedAd(
                            true
                        ) else null

                    Logger.log("9559595 previousAd =  " + previousAd)

                    if (previousAd != null) {
                        adSpace.visibility = View.GONE
                        view.visibility = View.VISIBLE
                    }

                    var isnewAd = false
                    val adData = adsManager.loadAdmobNativeAd(null, true, previousAd) { newAd ->
                        isnewAd = newAd
                    }?.let {
                        it
                    } ?: previousAd

                    if (bannerAdVisibilityHidden) return@launch
                    Logger.log("9559595 bannerAdVisibilityHidden =  " + bannerAdVisibilityHidden)
                    Logger.log("9559595 isnewAd =  " + isnewAd)
                    Logger.log("9559595 adData =  " + adData)
                    if (isnewAd) {
                        if (bannerAdVisibilityHidden) return@launch
                        adSpace.visibility = View.GONE
                        view.visibility = View.VISIBLE
                        if (adData != null) {
                            val newInstanceOfBannerAdPlaceholder =
                                BannerAdPlaceholder.getBannerPlaceholder(bannerAdPosition)
                            bannerAdPlaceHolderList[bannerAdPosition] =
                                newInstanceOfBannerAdPlaceholder
                            bannerAdList[bannerAdPosition] = adData
                            mediator?.detach()
                            viewPager2.adapter = null
                            viewPager2.adapter =
                                ViewPagerAdapter2(manager, lifecycle, bannerAdPlaceHolderList)
                            delay(250L)
                            viewPager2.currentItem = bannerAdPosition
                            mediator = TabLayoutMediator(tabLayout, viewPager2) { _, _ -> }
                            mediator?.attach()
                            bannerAdPosition++
                        }
                    } else if (viewPager2.adapter == null && adData != null) {
                        Logger.log("9559595 newwConsition =  ")
                        val newInstanceOfBannerAdPlaceholder =
                            BannerAdPlaceholder.getBannerPlaceholder(bannerAdPosition)
                        bannerAdPlaceHolderList[bannerAdPosition] = newInstanceOfBannerAdPlaceholder
                        bannerAdList[bannerAdPosition] = adData
                        viewPager2.adapter =
                            ViewPagerAdapter2(manager, lifecycle, bannerAdPlaceHolderList)
                        delay(250L)
                        viewPager2.currentItem = bannerAdPosition
                        mediator = TabLayoutMediator(tabLayout, viewPager2) { _, _ -> }
                        mediator?.attach()
                        bannerAdPosition++
                    }

                    adData?.let {
                        Logger.log("9559595 adData =  1...")
                        Logger.log("9559595 adData =  1. 1 .." + viewPager2.adapter?.itemCount)
                        if (bannerAdVisibilityHidden) return@launch
                        a?.setExpanded(true)
                        adSpace.visibility = View.GONE
                    } ?: kotlin.run {
                        if (bannerAdVisibilityHidden) return@launch
                        adSpace.visibility = View.VISIBLE
                        view.visibility = View.GONE
                        adsfreebt.visibility = View.VISIBLE
                    }
                }
            }
        } ?: kotlin.run {
            adSpace.visibility = View.GONE
            view.visibility = View.GONE
        }
    }

    var intervalCounter = 1
    fun handleNativeFull(
        executeFun: () -> Unit,
        activity: MainActivity,
        id: Int,
        showToolbar: Boolean = true,
        instantlyshowNativeInterstitialAdProgressBar: Boolean = true,
        afterInterstitialShown: (() -> Unit)? = null,
        showInIntervals: Boolean = false,
    ) {
        if (activityPausedByInterstitialAd) return

        if (showInIntervals) {
            val prevValue = intervalCounter
            intervalCounter++
            if (prevValue % 3 != 0) {
                afterInterstitialShown?.invoke()
                execute(executeFun)
                return
            }
        }

        CoroutineScope(Main).launch {
            if (adsPremiumPlanPurchased == null) {
                isAdsFreePlanPurchased()
            }
            if (adsPremiumPlanPurchased == true || adsPremiumPlanPurchased == null) {
                afterInterstitialShown?.invoke()
                execute(executeFun)
                return@launch
            }

            if (instantlyshowNativeInterstitialAdProgressBar) {
                activity.binding?.let {
                    it.nativeInterstitialAd.root.visibility = View.VISIBLE
                    it.nativeInterstitialAd.addl.visibility = View.GONE
                    it.nativeInterstitialAd.adloadingCover.visibility = View.VISIBLE
                    it.nativeInterstitialAd.adprogressbar.visibility = View.VISIBLE
                }
            }
            adsManager.initNativeFullAd()
            showInterstitialAd(
                executeFun = {
                    afterInterstitialShown?.invoke()
                    CoroutineScope(Main).launch {
                        showNativeFullAd(
                            executeFun = {
                                activityPausedByInterstitialAd = false
//                                adJob.complete()
                                executeFun.invoke()
                            }, activity, id, showToolbar,
                            instantlyshowNativeInterstitialAdProgressBar
                        )
                    }
                },
                activity, id, showToolbar
            )
        }
    }

    private fun showNativeFullAd(
        executeFun: () -> Unit,
        activity: MainActivity,
        id: Int,
        showToolbar: Boolean = true,
        instantlyshowNativeInterstitialAdProgressBar: Boolean,
    ) {
        activity.binding?.let {
            val job = Job()
            showNativeAd(
                it.nativeInterstitialAd.root,
                job,
                activity = activity,
                id, showToolbar, instantlyshowNativeInterstitialAdProgressBar
            )
            job.invokeOnCompletion {
                CoroutineScope(Main).launch {
                    execute(executeFun)
                }
            }
        }
    }

    private fun execute(executeFun: () -> Unit) {
        try {
            executeFun.invoke()
        } catch (_: Exception) {
        }
    }
}