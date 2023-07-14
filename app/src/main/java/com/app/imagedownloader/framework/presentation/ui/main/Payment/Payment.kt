package com.app.imagedownloader.framework.presentation.ui.main.Payment


import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.PremiumFeaturesService
import com.app.imagedownloader.Utils.PremiumFeaturesService.Companion.productsDetailsList
import com.app.imagedownloader.databinding.FragmentOnBoarding8Binding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.Adapters.OnBoardingStatePagerAdapter
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity.Companion.adsInfoLoadingStatus
import com.app.instastorytale.business.data.PremiumAccess.PremiumPlans
import com.app.instastorytale.framework.ViewPagerModifications.HorizontalMarginItemDecoration
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionLaunchCallback
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QLaunchResult
import com.qonversion.android.sdk.dto.QPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class Payment : Fragment(R.layout.fragment_on_boarding8) {
    lateinit var adapter: OnBoardingStatePagerAdapter
    val planPos = 0
    var purchaseChoosenByUser: PremiumPlans? = null
    var binding: FragmentOnBoarding8Binding? = null
    val scrollstatus: MutableLiveData<Boolean> = MutableLiveData()
    var paymentUnderProcessing = false

    @Inject
    lateinit var premiumFeaturesService: PremiumFeaturesService

    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

    companion object {
        var initFailed = false
        var initQonversionSdk: Channel<Boolean> = Channel()
        fun initQonversionSdk(application: Application) {
            Qonversion.launch(
                application,
                "vHJD1lQL_Gjymt78suvZF0AtBaGfzWZU",
                false,
                object : QonversionLaunchCallback {
                    override fun onError(error: QonversionError) {
                        Logger.log("Debug 8929 e = " + error.additionalMessage)
                        Logger.log("Debug 8929 e = " + error.description)
                        initFailed = true
                        CoroutineScope(Main).launch {
                            adsInfoLoadingStatus.emit(false)
                        }
                    }

                    override fun onSuccess(launchResult: QLaunchResult) {
                        Logger.log("Debug 8929 e = " + launchResult.permissions)
                        initFailed = false
                        CoroutineScope(Main).launch {
                            adsInfoLoadingStatus.emit(true)
                        }
                    }
                })
                Qonversion.restore(object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    Logger.log("Debug 8929 e 2 = " + error.additionalMessage)
                    Logger.log("Debug 8929 e 2 = " + error.description)
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {

                }
            })
        }
    }

    init {
        if (initFailed) {
            productsDetailsList.value = HashMap()
        }
    }

    override fun onStart() {
        super.onStart()
        generalAdsManager.activityPausedByInterstitialAd=false
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnBoarding8Binding.bind(view)

        loadPremiumPlans()
        handleClicks()

        lifecycleScope.launch(Main) {
            val currentPlan = premiumFeaturesService.checkUserSubscribedToAnyPlan(
                true
            )

            showPurchasedUi(
                currentPlan = currentPlan, showToast = false
            )
        }
        productsDetailsList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.keys.isNotEmpty()) {
                    binding?.shimmerLayout?.stopShimmer()
                    binding?.shimmerLayout1?.stopShimmer()
                    binding?.shimmerLayout?.visibility = View.GONE
                    binding?.shimmerLayout1?.visibility = View.GONE
                    binding?.buybt?.visibility = View.VISIBLE
                    setPrices()
                } else {
                    lifecycleScope.launch(IO) {
                        initQonversionSdk.send(true)
                        premiumFeaturesService.loadPricing()
                    }
                }
            } ?: kotlin.run {
                productsDetailsList.value = HashMap()
                binding?.buybt?.text = getString(R.string.planloaderror)
                binding?.buybt?.visibility = View.VISIBLE
                binding?.mcard?.visibility = View.GONE
                binding?.ycard?.visibility = View.GONE
            }
        }

        scrollstatus.observe(viewLifecycleOwner) {
            if (productsDetailsList.value?.keys?.isNotEmpty() == true && !paymentUnderProcessing) {
                binding?.let { binding ->
                    if (it) {
                        binding.mcard.visibility = View.VISIBLE
                        binding.ycard.visibility = View.VISIBLE
                        binding.buycard.visibility = View.VISIBLE
                        binding.savecard.visibility = View.VISIBLE
                    } else {
                        binding.buybt.text = getString(R.string.selectplan)
                        purchaseChoosenByUser = null
                        binding.yearlyplan.isEnabled = false
                        binding.monthlyplan.isEnabled = false
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleClicks() {
        binding!!.ycard.setOnClickListener {
            if (paymentUnderProcessing) return@setOnClickListener
            when (planPos) {
                0 -> {
                    purchaseChoosenByUser = PremiumPlans.UnlockAdsFreeYearly
                }

                1 -> {
//                    purchaseChoosenByUser = PremiumPlans.UnlockGoogleDriveAndAdsFreeYearly
                }

                2 -> {
//                    purchaseChoosenByUser = PremiumPlans.UnlockGoogleDriveYearly
                }
            }
            binding!!.yearlyplan.isEnabled = true
            binding!!.monthlyplan.isEnabled = false
            binding!!.buybt.text = getString(R.string.buyannualplan)
        }

        binding!!.mcard.setOnClickListener {
            if (paymentUnderProcessing) return@setOnClickListener
            when (planPos) {
                0 -> {
                    purchaseChoosenByUser = PremiumPlans.UnlockAdsFreeMonthly
                }

                1 -> {
//                    purchaseChoosenByUser = PremiumPlans.UnlockGoogleDriveAndAdsFreeMonthly
                }

                2 -> {
//                    purchaseChoosenByUser = PremiumPlans.UnlockGoogleDriveMonthly
                }
            }
            binding!!.yearlyplan.isEnabled = false
            binding!!.monthlyplan.isEnabled = true
            binding!!.buybt.text = getString(R.string.buymonthlyplan)
        }

        binding!!.buycard.setOnClickListener {
            if (purchaseChoosenByUser != null) {
                paymentUnderProcessing = true
                showBuyingPb()
                lifecycleScope.launch(IO) {
                    val existingPremiumPlan =
                        premiumFeaturesService.checkUserSubscribedToAnyPlan(true)
                    if (existingPremiumPlan != PremiumPlans.FreePlan && existingPremiumPlan != PremiumPlans.Error) {
                        if (existingPremiumPlan == purchaseChoosenByUser) {
                            premiumFeaturesService.purchasePremiumPlan(
                                requireActivity(), purchaseChoosenByUser!!
                            )
                            withContext(Main) {
                                hideBuyingPb()
                            }
                        } else {
                            upgrade_downgrade_plan(
                                oldProduct = existingPremiumPlan,
                                newProduct = purchaseChoosenByUser!!
                            )
                        }
                    } else if (existingPremiumPlan == PremiumPlans.Error) {
                        withContext(Main) {
                            Toast.makeText(
                                requireContext(),
                                "Unknown Error Try Again",
                                Toast.LENGTH_LONG
                            ).show()

                            hideBuyingPb()
                        }
                    } else {
                        val purchaseStatus = premiumFeaturesService.purchasePremiumPlan(
                            requireActivity(), purchaseChoosenByUser!!
                        )

                        Logger.log("64899889  5 ........       =  " + purchaseStatus)

                        if (purchaseStatus == -5) {
                            withContext(Main) {
                                showPlayRechargeCodeMessage()
                            }
                        }
                        if (purchaseStatus == 1) {
                            withContext(Main) {
                                showPurchasedUi(purchaseChoosenByUser!!, true)
                            }
                        }
                        withContext(Main) {
                            hideBuyingPb()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Select a Plan", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showBuyingPb() {
        binding?.buyingpb?.visibility = View.VISIBLE
        binding?.buycard?.visibility = View.INVISIBLE
    }

    private fun hideBuyingPb() {
        binding?.buyingpb?.visibility = View.GONE
        binding?.buycard?.visibility = View.VISIBLE
        binding!!.yearlyplan.isEnabled = false
        binding!!.monthlyplan.isEnabled = false
        binding!!.buybt.text = getString(R.string.selectplan)
        purchaseChoosenByUser = null
        paymentUnderProcessing = false
    }

    private fun upgrade_downgrade_plan(oldProduct: PremiumPlans, newProduct: PremiumPlans) {
        Qonversion.updatePurchase(
            requireActivity(),
            productId = newProduct.playStoreId,
            oldProductId = oldProduct.playStoreId,
            prorationMode = IMMEDIATE_WITH_TIME_PRORATION,
            object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    Logger.log("64899889 3 0 0 ........    error =     ")
                    hideBuyingPb()
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {
                    if (permissions[newProduct.playStoreId]?.isActive() == true) {
                        CoroutineScope(Main).launch {
                            showPurchasedUi(newProduct, true)
                            hideBuyingPb()
                        }
                    }
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun showPurchasedUi(currentPlan: PremiumPlans, showToast: Boolean = false) {
        if (currentPlan != PremiumPlans.Error && currentPlan != PremiumPlans.FreePlan) {
            binding?.planstatus?.text = "Current Plan : ${currentPlan.uiValue}"
        }

        if (showToast) {
            updatePurchaseStatus(
                currentPlan.uiValue
            )
        }
    }


    private fun updatePurchaseStatus(planid: String) {
        Toast.makeText(
            requireContext(),
            "Purchased ${planid} successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPlayRechargeCodeMessage() {
        MaterialDialog(requireContext()).show {
            setContentView(R.layout.rechargebyplaycode)

        }.findViewById<TextView>(R.id.wherebuy).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://play.google.com/intl/en_in/about/giftcards/#where-to-buy")
            }
            requireContext().startActivity(intent)
        }
    }

    private fun loadPremiumPlans() {
        adapter =
            OnBoardingStatePagerAdapter(
                lifecycle,
                manager = childFragmentManager,
                listOf(Offerui1())
            )
        binding?.viewpager2?.adapter = adapter

        binding?.viewpager2?.offscreenPageLimit = 1

        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx =
            resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * abs(position))
            // If you want a fading effect uncomment the next line:
            // page.alpha = 0.25f + (1 - abs(position))
        }
        binding?.viewpager2?.setPageTransformer(pageTransformer)

// The ItemDecoration gives the current (centered) item horizontal margin so that
// it doesn't occupy the whole screen width. Without it the items overlap
        val itemDecoration = HorizontalMarginItemDecoration(
            requireContext(),
            R.dimen.viewpager_current_item_horizontal_margin
        )
        binding?.viewpager2?.addItemDecoration(itemDecoration)
        TabLayoutMediator(binding!!.tablayout, binding!!.viewpager2) { _, _ ->

        }.attach()
    }

    @SuppressLint("SetTextI18n")
    private fun setPrices() {
        val listsize = productsDetailsList.value?.keys?.isNotEmpty()
        if (listsize == true) {
            when (planPos) {
                0 -> {
                    val unlockAdsFreeMonthlyPrice =
                        productsDetailsList.value?.get(PremiumPlans.UnlockAdsFreeMonthly.playStoreId)?.price
                    val unlockAdsFreeYearlyPrice =
                        productsDetailsList.value?.get(PremiumPlans.UnlockAdsFreeYearly.playStoreId)?.price

                    binding!!.monthlyplanprice.text =
                        "$unlockAdsFreeMonthlyPrice/Month"
                    binding!!.yearlyplanprice.text =
                        unlockAdsFreeYearlyPrice?.let {
                            monthlyPriceFromYearly(
                                it
                            )
                        }

                    unlockAdsFreeMonthlyPrice?.let { m ->
                        unlockAdsFreeYearlyPrice?.let { y ->
                            binding!!.savecard.visibility = View.VISIBLE
                            percentageSavedFromYearly(
                                monthlyPrice = m,
                                yearlyPrice = y
                            )?.let {
                                binding!!.savings.text = it
                            } ?: kotlin.run {
                                binding!!.savecard.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun monthlyPriceFromYearly(yearlyPrice: String): String {
        return try {
            val org = yearlyPrice
            val indexofFirstDigit = org.indexOfFirst { it.isDigit() }
            val currencySymbol = org.subSequence(0, indexofFirstDigit).toString()
            var after = ""
            val before = org.substringBefore(".").filter { it.isDigit() }
            if (org.contains(".")) {
                after = org.substringAfter(".").filter { it.isDigit() }

            }
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val num = (before + "." + after).toDouble().div(12.00)
            var final = df.format(num)

            if (!final.contains(".")) {
                final += ".00"
            }
            "$currencySymbol$final/Month"
        } catch (e: Exception) {
            "$yearlyPrice/Year"
        }
    }

    private fun percentageSavedFromYearly(monthlyPrice: String, yearlyPrice: String): String? {
        return try {
            val yearlyorg = yearlyPrice
            var yafter = ""
            val ybefore = yearlyorg.substringBefore(".").filter { it.isDigit() }
            if (yearlyorg.contains(".")) {
                yafter = yearlyorg.substringAfter(".").filter { it.isDigit() }
            }

            val monthlyorg = monthlyPrice
            var mafter = ""
            val mbefore = monthlyorg.substringBefore(".").filter { it.isDigit() }
            if (monthlyorg.contains(".")) {
                mafter = monthlyorg.substringAfter(".").filter { it.isDigit() }
            }
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.UP

            val ynum = (ybefore + "." + yafter).toDouble().div(12.00)
            val mnum = (mbefore + "." + mafter).toDouble()
            val diff = ((mnum - ynum).div(mnum)).times(100).roundToInt()

            "Save $diff%"
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}