package com.app.instastorytale.business.domain.Models.PremiumPlanModel

data class ProductDetailsInfo(
    val id: String,
    val monthly: Boolean,
    val price: String,
    val countryCode: String?
)