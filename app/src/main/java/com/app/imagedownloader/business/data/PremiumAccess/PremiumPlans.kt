package com.app.instastorytale.business.data.PremiumAccess

sealed class PremiumPlans(val uiValue:String,val playStoreId:String){


    object Error:PremiumPlans("billingClientError","billingClientError")

    object FreePlan:PremiumPlans("Free Plan","free")

    object UnlockAdsFreeMonthly:PremiumPlans("Ads-Free Monthly Plan","ads_free_plan")
    object UnlockAdsFreeYearly:PremiumPlans("Ads-Free Yearly Plan","ads_free_plan_yearly")

}
