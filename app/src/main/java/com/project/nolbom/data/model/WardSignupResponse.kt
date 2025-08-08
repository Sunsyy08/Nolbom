package com.project.nolbom.data.model

data class WardSignupResponse(
    val success: Boolean,
    val message: String?,
    val ward_id: Long? = null,
    val token: String? = null,              // 🆕 추가
    val user_id: Long? = null,              // 🆕 추가
    val name: String? = null,               // 🆕 추가
    val home_address: String? = null,       // 🆕 추가
    val profile_image: String? = null,      // 🆕 추가 (Base64)
    val error: String? = null
)
