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
import com.app.instastorytale.framework.ViewPagerModifications.HorizontalMarginItemDecoration
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.Utils.PremiumFeaturesService
import com.app.imagedownloader.Utils.PremiumFeaturesService.productsDetailsList
import com.app.imagedownloader.databinding.FragmentOnBoarding8Binding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager.Companion.adsPremiumPlanPurchased
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.Adapters.OnBoardingStatePagerAdapter
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity.Companion.adsInfoLoadingStatus
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionLaunchCallback
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QLaunchResult
import com.qonversion.android.sdk.dto.QPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Payment : Fragment(R.layout.fragment_on_boarding8) {
    lateinit var adapter: OnBoardingStatePagerAdapter
    var purchaseChoosenByUser = -1
    var binding: FragmentOnBoarding8Binding? = null
    val scrollstatus: MutableLiveData<Boolean> = MutableLiveData()
    var paymentUnderProcessing = false

    companion object {
        var initFailed = false
        var initQonversionSdk: Channel<Boolean> = Channel()
        fun initQonversionSdk(application: Application) {
            Qonversion.launch(application,
                "t4vFwKFcXWj1nkYqHg0VS_J5wukVQFhJ",
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnBoarding8Binding.bind(view)

        loadPremiumPlans()

        handleClicks()

        lifecycleScope.launch(Main) {
            showPurchasedUi(
                currentPlan = PremiumFeaturesService.checkUserSubscribedToAnyPlan(
                    true, requireContext()
                ), showToast = false
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
                        PremiumFeaturesService.loadPricing()
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
                        purchaseChoosenByUser = -1
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
            purchaseChoosenByUser = 4
            binding!!.yearlyplan.isEnabled = true
            binding!!.monthlyplan.isEnabled = false
            binding!!.buybt.text = getString(R.string.buyannualplan)
        }

        binding!!.mcard.setOnClickListener {
            if (paymentUnderProcessing) return@setOnClickListener
            purchaseChoosenByUser = 1
            binding!!.yearlyplan.isEnabled = false
            binding!!.monthlyplan.isEnabled = true
            binding!!.buybt.text = getString(R.string.buymonthlyplan)
        }

        binding!!.buycard.setOnClickListener {
            if (purchaseChoosenByUser != -1) {
                paymentUnderProcessing = true
                adsPremiumPlanPurchased = null
                showBuyingPb()
                lifecycleScope.launch(IO) {
                    val existingPremiumPlan =
                        PremiumFeaturesService.checkUserSubscribedToAnyPlan(true, requireContext())
                    if (existingPremiumPlan != 0 && existingPremiumPlan != -1) {
                        if (existingPremiumPlan == purchaseChoosenByUser) {
                            PremiumFeaturesService.purchasePremiumPlan(
                                requireActivity(), purchaseChoosenByUser
                            )
                            withContext(Main) {
                                hideBuyingPb()
                            }
                        } else {
                            upgrade_downgrade_plan(
                                oldProductId = PremiumFeaturesService.planId[existingPremiumPlan]!!,
                                newProductId = PremiumFeaturesService.planId[purchaseChoosenByUser]!!
                            )
                        }
                    } else if (existingPremiumPlan == -1) {
                        withContext(Main) {
                            Toast.makeText(
                                requireContext(), "Unknown Error Try Again", Toast.LENGTH_LONG
                            ).show()

                            hideBuyingPb()
                        }
                    } else {
                        val purchaseStatus = PremiumFeaturesService.purchasePremiumPlan(
                            requireActivity(), purchaseChoosenByUser
                        )

                        if (purchaseStatus == -5) {
                            withContext(Main) {
                                showPlayRechargeCodeMessage()
                            }
                        }
                        if (purchaseStatus == 1) {
                            withContext(Main) {
                                showPurchasedUi(purchaseChoosenByUser, true)
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
        purchaseChoosenByUser = -1
        paymentUnderProcessing = false
    }

    private fun upgrade_downgrade_plan(oldProductId: String, newProductId: String) {
        Qonversion.updatePurchase(requireActivity(),
            productId = newProductId,
            oldProductId = oldProductId,
            prorationMode = IMMEDIATE_WITH_TIME_PRORATION,
            object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    hideBuyingPb()
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {
                    if (permissions[newProductId]?.isActive() == true) {
                        val planId =
                            PremiumFeaturesService.planId.entries.filter { it.value == newProductId }[0].key
                        showPurchasedUi(
                            planId, true
                        )
                        hideBuyingPb()
                    }
                }
            })
    }

    private fun showPurchasedUi(currentPlan: Int, showToast: Boolean = false) {
        if (currentPlan != -1 && currentPlan != 0) {
            val purchasedId = currentPlan
            adsPremiumPlanPurchased = purchasedId == 1 || purchasedId == 4
            binding?.planstatus?.text =
                PremiumFeaturesService.map[PremiumFeaturesService.planId[currentPlan]]
        }

        if (showToast) {
            updatePurchaseStatus(
                PremiumFeaturesService.map[PremiumFeaturesService.planId[currentPlan]]!!
            )
        }
    }

    private fun updatePurchaseStatus(planid: String) {
        Toast.makeText(
            requireContext(), "Purchased ${planid} successfully", Toast.LENGTH_SHORT
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
        adapter = OnBoardingStatePagerAdapter(
            lifecycle, manager = childFragmentManager, listOf(Offerui1())
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
            requireContext(), R.dimen.viewpager_current_item_horizontal_margin
        )
        binding?.viewpager2?.addItemDecoration(itemDecoration)

        binding!!.viewpager2.setCurrentItem(1, false)

        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == 0) {
                    scrollstatus.postValue(true)
                } else {
                    scrollstatus.postValue(false)
                }
            }
        }
        binding?.viewpager2?.registerOnPageChangeCallback(callback)
    }

    @SuppressLint("SetTextI18n")
    private fun setPrices() {
        val listsize = PremiumFeaturesService.productsDetailsList.value?.keys?.isNotEmpty()
        if (listsize == true) {
            binding!!.monthlyplanprice.text =
                PremiumFeaturesService.productsDetailsList.value?.get(Constants.PLAN_1)?.price + "/Month"
            binding!!.yearlyplanprice.text =
                PremiumFeaturesService.productsDetailsList.value?.get(Constants.PLAN_4)?.price?.let {
                    monthlyPriceFromYearly(
                        it
                    )
                }

            PremiumFeaturesService.productsDetailsList.value?.get(Constants.PLAN_1)?.price?.let { m ->
                PremiumFeaturesService.productsDetailsList.value?.get(Constants.PLAN_4)?.price?.let { y ->
                    binding!!.savecard.visibility = View.VISIBLE
                    percentageSavedFromYearly(
                        monthlyPrice = m, yearlyPrice = y
                    )?.let {
                        binding!!.savings.text = it
                    } ?: kotlin.run {
                        binding!!.savecard.visibility = View.GONE
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