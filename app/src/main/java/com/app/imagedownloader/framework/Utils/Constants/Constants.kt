package com.app.imagedownloader.Utils.Constants

import com.android.volley.DefaultRetryPolicy


object Constants {
    const val MAIN_SHARED_PREFS = "MAIN_SHARED_PREFS"

    const val FCM_NOTIFICATION_ID = "456"
    const val FCM_NOTIFICATION_CHANNEL = "General"
    const val FCM_COMMON_TOPIC = "general"
    const val FCM_COMMON_SUBSCRIBED = "FCM_COMMON_SUBSCRIBED"
    const val JSON_ERROR = "JSON_ERROR"
    const val ON_BOARDING = "ON_BOARDING"
    const val PLAYER_AD_REPEAT_INTERVAL = 4

    //Retry Policy
    val RetryPolicy = DefaultRetryPolicy(10000, 2, 1F)

    //////////////// ProductId
    const val PLAN_base1 = "base-premium-plan-1"
    const val PLAN_base2 = "base-premium-plan-2"
    const val PLAN_base3 = "base-premium-plan-3"
    const val PLAN_base4 = "base-premium-plan-yearly-1"
    const val PLAN_base5 = "base-premium-plan-yearly-2"
    const val PLAN_base6 = "base-premium-plan-yearly-3"

    const val PLAN_0 = "free"
    const val PLAN_1 = "premium_plan_1"
    const val PLAN_2 = "premium_plan_2"
    const val PLAN_3 = "premium_plan_3"
    const val PLAN_4 = "premium_plan_1_yearly"
    const val PLAN_5 = "premium_plan_2_yearly"
    const val PLAN_6 = "premium_plan_3_yearly"

}