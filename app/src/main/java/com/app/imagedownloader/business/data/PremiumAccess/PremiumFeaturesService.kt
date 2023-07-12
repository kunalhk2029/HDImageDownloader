package com.app.imagedownloader.Utils

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity.Companion.adsInfoLoadingStatus
import com.app.instastorytale.business.data.PremiumAccess.PremiumPlans
import com.app.instastorytale.business.domain.Models.PremiumPlanModel.ProductDetailsInfo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonParser
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionOfferingsCallback
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.offerings.QOfferings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PremiumFeaturesService
@Inject constructor(private val context: Context) {

    private lateinit var billingClient: BillingClient

    companion object {
        var productsDetailsList: MutableLiveData<HashMap<String, ProductDetailsInfo>?> =
            MutableLiveData(
                HashMap()
            )
    }

    private fun initBillingClient() {
        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                purchases?.forEach {
                    Logger.log("Debug 8929  purchase state okl   = " + it.purchaseState)
                    Logger.log("Debug 8929  purchase ack    = " + it.isAcknowledged)
                }
            }


        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                 Logger.log("Debug 8929 billing code = "+billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
//                    querySkuDetails(billingClient)
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private suspend fun checkActivePremiumPlanIdByQonversionApi(): PremiumPlans {
        var plan: PremiumPlans? = null
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onError(error: QonversionError) {
                plan = PremiumPlans.Error
            }

            override fun onSuccess(permissions: Map<String, QPermission>) {
            }
        })
        while (plan == null) {
            delay(1000L)
        }
        return plan!!
    }

    private suspend fun checkActivePremiumPlanIdByGoogleBillingApi(
        billingClient: BillingClient
    ): PremiumPlans {
        var plan: PremiumPlans? = null
        withContext(Dispatchers.IO) {
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.SUBS
            ) { p0, p1 ->
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {

                    val availablePlans = listOf(
                        PremiumPlans.UnlockAdsFreeMonthly,
                        PremiumPlans.UnlockAdsFreeYearly)

                    for (availablePlan in availablePlans) {
                        val planPurchasedInfo = p1.filter {
                            JsonParser.parseString(it.originalJson).asJsonObject.get("productId").asString ==
                                    availablePlan.playStoreId
                        }

                        if (planPurchasedInfo.isNotEmpty()) {
                            verifyPurchasedIsAcknowledged(
                                planPurchasedInfo,
                                availablePlan
                            )?.let {
                                plan = it
                            }
                        }
                        if (plan != null) break
                    }

                    if (plan == null) {
                        plan = PremiumPlans.FreePlan
                    }
                } else {
                    logAdStatusFetchError()
                    plan = PremiumPlans.Error
                }
            }
        }
        while (plan == null) {
            delay(1000L)
        }

        return plan!!
    }

    private fun verifyPurchasedIsAcknowledged(
        purchaseList: List<Purchase>,
        plan: PremiumPlans
    ): PremiumPlans? {
        val planId =
            JsonParser.parseString(purchaseList.first().originalJson).asJsonObject.get("productId").asString
        if (planId != plan.playStoreId) throw  Exception("Wrong Premium Plan passed in the parameter")
        val purchased =
            purchaseList[0].purchaseState == Purchase.PurchaseState.PURCHASED
        val ack = purchaseList[0].isAcknowledged
        return if (purchased && ack) plan else null
    }

    private fun logAdStatusFetchError() {
        FirebaseCrashlytics.getInstance()
            .log("PremiumFeaturesService checkAdsPlanPurchased Error")
        try {
            throw Exception("Exception message : PremiumFeaturesService checkAdsPlanPurchased pid = -1")
        } catch (e: Exception) {
            e.message?.let {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }


    suspend fun purchasePremiumPlan(activity: Activity, plan: PremiumPlans): Int {
        // -5 for cancelling by user
        var completed = -2
        Qonversion.purchase(
            context = activity,
            plan.playStoreId,
            object : QonversionPermissionsCallback {
                override fun onError(error: QonversionError) {
                    completed = -1
                    Logger.log("Debug 8929 calback 1  error = " + error.additionalMessage)
                    Logger.log("Debug 8929 calback 1  error = " + error.description)

                    if (error.code.toString() == "CanceledPurchase" || error.description.contains("User pressed back")) {
                        completed = -5
                    }
                }

                override fun onSuccess(permissions: Map<String, QPermission>) {
                    completed = 0
                    if (permissions[plan.playStoreId]?.isActive() == true) {
                        completed = 1
                    }
                }
            })
        while (completed == -2) {
            delay(1000L)
        }
        return completed
    }

    suspend fun checkUserSubscribedToAnyPlan(
        checkUsingBillingLibrary: Boolean,
    ): PremiumPlans {
//         Logger.log("Debug 8929 checkUserSubscribedToAnyPlan called.............. 1 ")
        lateinit var plan: PremiumPlans
        withContext(Default) {
            val job = launch {
                if (checkUsingBillingLibrary) {
                    initBillingClient()
                    while (billingClient.connectionState == BillingClient.ConnectionState.CONNECTING) {
                        delay(500L)
                    }
                    Logger.log("Debug 8929 billing state =" + billingClient.connectionState)

                    if (billingClient.connectionState == BillingClient.ConnectionState.DISCONNECTED) {
                        plan = PremiumPlans.Error
                        return@launch
                    }
                }
            }
            job.join()
            val job1 = launch {
                if (checkUsingBillingLibrary) {
                    if (billingClient.isReady) {
                        plan = checkActivePremiumPlanIdByGoogleBillingApi(billingClient)
                    } else {
                        logAdStatusFetchError()
                        plan = PremiumPlans.Error
                    }
                } else {
                    plan = checkActivePremiumPlanIdByGoogleBillingApi(billingClient)
                }
            }
            job1.join()
        }
//         Logger.log("Debug 8929 checkUserSubscribedToAnyPlan returned.............. 1 ")

        return plan
    }

    suspend fun loadPricing() {
        adsInfoLoadingStatus.asSharedFlow().collectLatest {
            if (it) {
                val map: HashMap<String, ProductDetailsInfo> = HashMap()
                Qonversion.offerings(object : QonversionOfferingsCallback {
                    override fun onError(error: QonversionError) {
                        Logger.log("Debug error =  .................. " + error.additionalMessage)
                        productsDetailsList.postValue(null)
                    }

                    override fun onSuccess(offerings: QOfferings) {
                        offerings.availableOfferings.forEach {
                            it.products.forEach {
                                Logger.log("Debug  ghjkliop .................. = " + it.qonversionID)
                                map[it.qonversionID] = ProductDetailsInfo(
                                    id = it.qonversionID,
                                    monthly = it.duration.toString() == "Monthly",
                                    price = it.prettyPrice.toString(),
                                    countryCode = it.skuDetail?.priceCurrencyCode
                                )
                            }
                        }
                        productsDetailsList.postValue(map)
                    }
                })
            } else {
                productsDetailsList.postValue(null)
            }
        }
    }


    suspend fun isAdsFreePlanPurchased(): Boolean? {
        val purchasedPlan = checkUserSubscribedToAnyPlan(true)
        return if (
            purchasedPlan == PremiumPlans.UnlockAdsFreeMonthly ||
            purchasedPlan == PremiumPlans.UnlockAdsFreeYearly
        ) true
        else if (purchasedPlan == PremiumPlans.Error) null
        else false
    }

    suspend fun getPurchasedPlan(): PremiumPlans {
        return checkUserSubscribedToAnyPlan(true)
    }
}

