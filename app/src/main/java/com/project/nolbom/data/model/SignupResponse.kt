package com.project.nolbom.data.model

data class SignupResponse(
    val success: Boolean,
    val user_id: Long,
    val token: String? = null,      // 🆕 추가
    val name: String? = null,       // 🆕 추가
    val email: String? = null,      // 🆕 추가
    val message: String? = null,
    val error: String? = null
)
