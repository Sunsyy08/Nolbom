package com.project.nolbom.data.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

// data/model/WardSignupRequest.kt
data class WardSignupRequest(
    val height: Float,
    val weight: Float,

    @Json(name = "medical_status")
    val medicalStatus: String,

    @Json(name = "home_address")
    val homeAddress: String,

    @Json(name = "safe_lat")
    val safeLat: Double,

    @Json(name = "safe_lng")
    val safeLng: Double,

    @Json(name = "safe_radius")
    val safeRadius: Int
)

