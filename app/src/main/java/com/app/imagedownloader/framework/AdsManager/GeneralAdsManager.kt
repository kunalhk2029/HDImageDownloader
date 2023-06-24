package com.app.imagedownloader.framework.AdsManager

import android.annotation.SuppressLint
import android.content.Context
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
import com.app.imagedownloader.Utils.Constants.Constants.PLAYER_AD_REPEAT_INTERVAL
import com.app.imagedownloader.Utils.PremiumFeaturesService
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.app.instastorytale.framework.presentation.Adapters.ViewPagerAdapter2
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableSharedFlow


class GeneralAdsManager(
    val adsManager: AdsManager,
    private val context: Context,
    private val sharedPrefRepository: SharedPrefRepository,
) {

    init {
        bannerAdList[0] = BannerAdPlaceholder.getBannerPlaceholder(0)
        bannerAdList[1] = BannerAdPlaceholder.getBannerPlaceholder(1)
    }

    companion object {
        var bannerAdVisibilityHidden = false
        var adsPremiumPlanPurchased: Boolean? = null
        val pausePlayerForAd: MutableSharedFlow<Boolean> = MutableSharedFlow()
        var bannerAdPosition = -1
        val bannerAdList: HashMap<Int, BannerAdPlaceholder> = HashMap()
    }

    private var initializing = false

    private suspend fun checkAdsPremiumPlan(context: Context): Boolean? {
        if (adsPremiumPlanPurchased != null) return adsPremiumPlanPurchased
        adsPremiumPlanPurchased = PremiumFeaturesService.checkAdsPlanPurchased(context)
        return adsPremiumPlanPurchased
    }

    private fun showNativeAd(
        view: View,
        job: CompletableJob,
        constrainedJob: CompletableJob,
        lateReadyJob: CompletableJob,
        retryAdDisplayJob: CompletableJob, activity: MainActivity, id: Int, showToolbar: Boolean,
    ) {
        if (adsPremiumPlanPurchased == true || adsPremiumPlanPurchased == null) {
            job.complete()
            return
        }
        val adloadingob = view.findViewById<ProgressBar>(R.id.adprogressbar)
        val adcover = view.findViewById<LinearLayout>(R.id.adloadingCover)
        val addl = view.findViewById<ConstraintLayout>(R.id.addl)
        val adl: ConstraintLayout = view as ConstraintLayout
        addl.visibility = View.GONE
        adl.visibility = View.VISIBLE
        adloadingob.visibility = View.VISIBLE
        adcover.visibility = View.VISIBLE
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
                    adsManager.loadAdmobNativeAd(template, false, null, null)
                adcover.visibility = View.GONE
                adloadingob.visibility = View.GONE
                adMobNativeAd?.let { nativeAd ->
                    Logger.log("8956858895 12 56")
                    handleNativeFullUi(
                        template,
                        view,
                        constrainedJob,
                        retryAdDisplayJob,
                        lateReadyJob,
                        id,
                        activity,
                        nativeAd,
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
        constrainedJob: CompletableJob,
        retryAdDisplayJob: CompletableJob,
        lateReadyJob: CompletableJob,
        id: Int,
        activity: MainActivity,
        admobNativeAd: NativeAd?,
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
        var sendPlayerPauseEvent = false
        if (constrainedJob.isCompleted == false) {
            constrainedJob.complete()
            retryAdDisplayJob.complete()
        } else {
            sendPlayerPauseEvent = true
            adl.visibility = View.GONE
            lateReadyJob.complete()
            Logger.log("Debug 65292929 constrained job is completed by ui.........")
        }
        retryAdDisplayJob.invokeOnCompletion {
            activity.hideToolbar()
            adl.visibility = View.VISIBLE
            template?.visibility = View.VISIBLE
            adclose.setOnClickListener {
                if (showToolbar) activity.showToolbar()
                if (currentFragId == id) {
                    activity.binding?.badd?.root?.visibility = View.VISIBLE
                }
                adl.visibility = View.GONE
//                admobNativeAd?.destroy()
//                facebookAd?.destroy()
                if (sendPlayerPauseEvent) {
                    sendPlayerPauseEvent = false
                    CoroutineScope(Main).launch {
                        pausePlayerForAd.emit(false)
                    }
                }
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
    }

    var currentFragId = -1

    suspend fun showInterstitialAd(): InterstitialAd? {
        if (adsPremiumPlanPurchased == null) {
            if (!initializing) {
                initiatingPaymentStatus()
            }
        }
        adsPremiumPlanPurchased?.let {
            if (it) {
                return null
            }
            return adsManager.getpreLoadedInterstitialAd(true)
        }
        return null
    }


    suspend fun showNativeAdapterItemAd(
        view: NativeAdView, root: View,
    ) {

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return
        }
        if (adsPremiumPlanPurchased == null) {
            if (!initializing) {
                initiatingPaymentStatus()
            }
        }
        adsPremiumPlanPurchased?.let {
            if (it) {
                view.visibility = View.GONE
                return@let
            }
            view.visibility = View.VISIBLE
            println("989898  / .......")
            adsManager.loadAdmobAdapterItemAd(view, itemRootView = root)
        } ?: kotlin.run {
            view.visibility = View.GONE
        }
    }

    suspend fun showNativeHomeScreenAd(
        view: NativeAdView,
    ) {

        println("989898  / .....55..")

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return
        }
        if (adsPremiumPlanPurchased == null) {
            if (!initializing) {
                initiatingPaymentStatus()
            }
        }
        adsPremiumPlanPurchased?.let {
            if (it) {
                view.visibility = View.GONE
                return@let
            }
            view.visibility = View.VISIBLE
            println("989898  / .......")
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

        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            return
        }

        if (adsPremiumPlanPurchased == null) {
            if (!initializing) {
                initiatingPaymentStatus()
            }
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
                    viewPager2.adapter = ViewPagerAdapter2(manager, lifecycle, bannerAdList)
                    TabLayoutMediator(tabLayout, viewPager2) { _, _ -> }.attach()
                }
                if (adsManager.getAdsServiceCompany()) {
                    val previousAd: NativeAd? =
                        if (adsManager.getpreLoadedAd(true)?.callToAction != null) adsManager.getpreLoadedAd(
                            true
                        ) else null
                    Logger.log("Debug admob Ads //getpreLoadedAd ........" + previousAd?.body)

                    var templateView: TemplateView? = null
                    if (previousAd != null) {
                        adSpace.visibility = View.GONE
                        view.visibility = View.VISIBLE
                    }
                    val adData = adsManager.loadAdmobNativeAd(null, true, previousAd) { newAd ->
                        if (bannerAdVisibilityHidden) return@loadAdmobNativeAd null
                        bannerAdList[if (bannerAdPosition == -1) 0 else bannerAdPosition]!!.let {
                            try {
                                if (adsManager.getpreLoadedAd(true) != null) {
                                    adSpace.visibility = View.GONE
                                    view.visibility = View.VISIBLE
                                }
                                if (!newAd && bannerAdPosition != -1) return@loadAdmobNativeAd null
                                if (bannerAdPosition == -1) bannerAdPosition = 0
                                viewPager2.setCurrentItem(bannerAdPosition)
                                delay(250L)
                                it.adpspace.visibility = View.GONE
                                it.templateView.visibility = View.VISIBLE
                                templateView = it.templateView
                                bannerAdPosition++
                                if (bannerAdPosition == 2) {
                                    bannerAdPosition = 0
                                }
                                templateView
                            } catch (e: Exception) {
                                FirebaseCrashlytics.getInstance()
                                    .log("showNativeBannerAd Template View Not Initialized")
                                try {
                                    throw Exception("Exception message : showNativeBannerAd Template View Not Initialized")
                                } catch (e: Exception) {
                                    e.message?.let {
                                        FirebaseCrashlytics.getInstance().recordException(e)
                                    }
                                }
                                null
                            }
                        }
                    }?.let {
                        it
                    } ?: previousAd

                    if (bannerAdVisibilityHidden) return@launch
                    adData?.let {
                        if (adSpace.visibility == View.VISIBLE) a?.setExpanded(true)
                        adSpace.visibility = View.GONE
                    } ?: kotlin.run {
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

    fun handleNativeFull(
        executeFun: () -> Unit,
        activity: MainActivity,
        id: Int,
        showToolbar: Boolean = true,
        forceDisplayingAdFirst: Boolean = false,
        afterInterstitialShown: (() -> Unit)? = null,
        showBoth: Boolean = true,
    ) {


        if (sharedPrefRepository.get_DisableAdsAndPromo()) {
            execute(executeFun)
            return
        }
        CoroutineScope(Main).launch {
            if (adsPremiumPlanPurchased == null) {
                Logger.log("84618941869169818   /// 1............")
                execute(executeFun)
                if (!initializing) {
                    initiatingPaymentStatus()
                }
                return@launch
            } else if (adsPremiumPlanPurchased == true) {
                Logger.log("84618941869169818 155............")
                Logger.log("84618941869169818   /// 2............")
                execute(executeFun)
                return@launch
            }


            Logger.log("84618941869169818 3............")

            if (showBoth) {
                val adJob= {
                    afterInterstitialShown?.invoke()
                    CoroutineScope(Main).launch {
                        showNativeFullAd(
                            executeFun,
                            activity,
                            id,
                            showToolbar,
                            forceDisplayingAdFirst
                        )
                    }
                }
                showInterstitialAd(
                   executeFun= { adJob.invoke() },
                    activity,executeOnErrorFun = { adJob.invoke()}
                )
            } else {
                showInterstitialAd({
                    executeFun.invoke()
                },
                    activity, executeOnErrorFun = {
                        CoroutineScope(Main).launch {
                            showNativeFullAd(
                                executeFun,
                                activity,
                                id,
                                showToolbar,
                                forceDisplayingAdFirst
                            )
                        }
                    }
                )
            }
            adsManager.initNativeAdsPreload()
        }
    }

    private suspend fun showInterstitialAd(
        executeFun: () -> Unit,
        activity: MainActivity,
        executeOnErrorFun: (() -> Unit)? = null
    ) {
        val ad = adsManager.getpreLoadedInterstitialAd(false)
        ad?.fullScreenContentCallback = object :
            FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                Logger.log("55666595 ............  // //  showFullScreenInterstitialAd = error  " + p0.message)
                executeOnErrorFun?.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                executeFun.invoke()
            }
        }
        ad?.show(activity)
    }


    private suspend fun showNativeFullAd(
        executeFun: () -> Unit,
        activity: MainActivity,
        id: Int,
        showToolbar: Boolean = true,
        forceDisplayingAdFirst: Boolean = false,
    ) {
        activity.binding?.let {
            val job = Job()
            val constrainedJob = Job()
            val lateReadyJob = Job()
            val retryAdDisplayJob = Job()
            showNativeAd(
                it.nativeInterstitialAd.root,
                job,
                constrainedJob = constrainedJob,
                lateReadyJob = lateReadyJob,
                retryAdDisplayJob = retryAdDisplayJob,
                activity = activity,
                id, showToolbar
            )

            job.invokeOnCompletion {
                CoroutineScope(Main).launch {
                    Logger.log("484989898   /// 5............")
                    execute(executeFun)
                }
            }
            withContext(IO) {
                if (!forceDisplayingAdFirst) {
                    val timeOut = withTimeoutOrNull(5000L) {
                        while (!constrainedJob.isCompleted) {
                            delay(500L)
                        }
                    }
                    if (timeOut == null) {
                        withContext(Main) {
                            it.nativeInterstitialAd.root.visibility = View.GONE
                        }
                        constrainedJob.complete()
                        job.complete()
                    }
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

    private suspend fun initiatingPaymentStatus(): Boolean {
        adsPremiumPlanPurchased = false
        return false
        checkAdsPremiumPlan(context)
        return true
    }
}