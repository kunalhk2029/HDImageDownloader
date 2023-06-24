package com.app.imagedownloader.Utils

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity.Companion.adsInfoLoadingStatus
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

object PremiumFeaturesService {

    data class ProductDetailsInfo(
        val id: String,
        val monthly: Boolean,
        val price: String,
        val countryCode: String?
    )

    val planId = HashMap<Int, String>()
    val map = HashMap<String, String>()

    init {
        map[Constants.PLAN_0] = "Free Plan"
        map[Constants.PLAN_1] = "Monthly Premium Plan"
        map[Constants.PLAN_4] = "Annual Premium Plan"

        ///////

        planId[0] = Constants.PLAN_0
        planId[1] = Constants.PLAN_1
        planId[4] = Constants.PLAN_4
    }

    private lateinit var billingClient: BillingClient
    var productsDetailsList: MutableLiveData<HashMap<String, ProductDetailsInfo>?> =
        MutableLiveData(
            HashMap()
        )


    private fun initBillingClient(context: Context) {
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

    private suspend fun checkActivePremiumPlanIdByQonversionApi(): Int {
        var status = -2

        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onError(error: QonversionError) {
                status = -1
                Logger.log("Debug 8929 calback 1///  error = " + error.additionalMessage)
                Logger.log("Debug 8929 calback 1///  error = " + error.description)
            }

            override fun onSuccess(permissions: Map<String, QPermission>) {
                Logger.log("Debug 8929 cd 202 returned..............   = " + permissions.keys)

                if (permissions[Constants.PLAN_1] != null && permissions[Constants.PLAN_1]?.isActive() == true) {
                    status = 1
                    return
                } else if (permissions[Constants.PLAN_4] != null && permissions[Constants.PLAN_4]?.isActive() == true) {
                    status = 4
                    return
                } else {
                    status = 0
                    return
                }
            }
        })
        while (status == -2) {
//             Logger.log("Debug 8929 cd 5 returned..............  ")
            delay(1000L)
        }

        return status
    }

    private suspend fun checkActivePremiumPlanIdByGoogleBillingApi(
        billingClient: BillingClient
    ): Int {
        var pid = -2
        withContext(Dispatchers.IO) {
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.SUBS
            ) { p0, p1 ->
                if (p0.responseCode == BillingClient
                        .BillingResponseCode.OK
                ) {

                    var checkId = 1
                    var mlist =
                        p1.filter { JsonParser.parseString(it.originalJson).asJsonObject.get("productId").asString == planId[1] }

                    if (mlist.isEmpty()) {
                        checkId = 4
                        mlist =
                            p1.filter { JsonParser.parseString(it.originalJson).asJsonObject.get("productId").asString == planId[4] }
                    }
                    if (mlist.isNotEmpty()) {
                        val purchased =
                            mlist.get(0).purchaseState == Purchase.PurchaseState.PURCHASED
                        val ack = mlist.get(0).isAcknowledged
                        Logger.log("Debug 8929 isAcknowledged = " + ack)
//                         Logger.log("Debug 8929 isAcknowledged = " + mlist[0].purchaseToken)
                        if (purchased && ack) {
                            pid = checkId
                            return@queryPurchasesAsync
                        }
                    }
                    pid = 0
                } else {
                    pid = -1
                }
            }
        }
        while (pid == -2) {
            delay(1000L)
        }
        Logger.log("Debug 8929 query purchased return int  =.............. " + pid)
        return pid
    }

    suspend fun purchasePremiumPlanByBillingApi(activity: Activity, planid: Int) {
        // -5 for cancelling by user
        //            lifecycleScope.launch(IO) {
//
//                PremiumFeaturesService.initBillingClient(requireContext())
//                while (PremiumFeaturesService.billingClient.connectionState == BillingClient.ConnectionState.CONNECTING) {
//                    delay(500L)
//                }
////                     Logger.log("Debug 8929 connection state ............ = "+ billingClient.connectionState)
//                if (PremiumFeaturesService.billingClient.connectionState == BillingClient.ConnectionState.DISCONNECTED) {
//                    return@launch
//                }
//                val skusetailsParams = SkuDetailsParams.newBuilder()
//                    .setSkusList(listOf(Constants.PLAN_3))
//                    .setType(BillingClient.SkuType.SUBS)
//                    .build()
//                PremiumFeaturesService.billingClient.querySkuDetailsAsync(skusetailsParams,
//                    object : SkuDetailsResponseListener {
//                        override fun onSkuDetailsResponse(
//                            p0: BillingResult,
//                            p1: MutableList<SkuDetails>?
//                        ) {
//                            if (p1?.isNotEmpty() == true) {
//                                val billingFlowParams = BillingFlowParams.newBuilder()
//                                    .setSkuDetails(p1[0]).build()
//                                PremiumFeaturesService.billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
//                            }
//                        }
//                    })
//            }
    }

    suspend fun purchasePremiumPlan(activity: Activity, planid: Int): Int {
        // -5 for cancelling by user
        var completed = -2
        Qonversion.purchase(
            context = activity,
            planId[planid]!!,
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
                    if (permissions[planId[planid]]?.isActive() == true) {
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
        context: Context?
    ): Int {
//         Logger.log("Debug 8929 checkUserSubscribedToAnyPlan called.............. 1 ")
        var status = -2
        withContext(Default) {
            val job = launch {
                if (checkUsingBillingLibrary) {
                    initBillingClient(context!!)
                    while (billingClient.connectionState == BillingClient.ConnectionState.CONNECTING) {
                        delay(500L)
                    }
                    Logger.log("Debug 8929 billing state =" + billingClient.connectionState)

                    if (billingClient.connectionState == BillingClient.ConnectionState.DISCONNECTED) {
                        status = -1
                        return@launch
                    }
                }
            }
            job.join()
            val job1 = launch {
                if (checkUsingBillingLibrary) {
                    if (billingClient.isReady) {
                        status = checkActivePremiumPlanIdByGoogleBillingApi(billingClient)
                    } else {
                        status = -1
                    }
                } else {
                    status = checkActivePremiumPlanIdByQonversionApi()
                }
            }
            job1.join()
        }
//         Logger.log("Debug 8929 checkUserSubscribedToAnyPlan returned.............. 1 ")

        return status
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
                                Logger.log("Debug ghjkliop .................. = " + it.qonversionID)

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

    suspend fun checkAdsPlanPurchased(context: Context): Boolean? {
        val purchasedId = checkUserSubscribedToAnyPlan(true, context)
        if (purchasedId == -1) return null
        else {
            return (purchasedId == 1
                    || purchasedId == 4
                    )
        }
    }
}

