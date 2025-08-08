// data/model/UserProfile.kt
package com.project.nolbom.data.model

import com.google.gson.annotations.SerializedName

// 🔧 이름을 바꿔서 충돌 방지
data class ProfileUserData(
    val name: String,
    val email: String,
    @SerializedName("userType") val userType: String,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    val address: String?,
    @SerializedName("profileImage") val profileImage: String?
)

data class ProfileResponse(
    val success: Boolean,
    val profile: ProfileUserData?,
    val error: String?
)