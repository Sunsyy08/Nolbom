package com.project.nolbom.data.model

data class GuardianResponse(
    val success: Boolean,
    val message: String?,
    val guardianId: Long? = null,
    val token: String? = null,      // 🆕 추가
    val address: String? = null,    // 🆕 추가
    val error: String? = null
)
