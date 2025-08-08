package com.project.nolbom.data.model

// 1. 데이터 모델 클래스들 (models/UserProfile.kt)
data class UserProfile(
    val name: String,
    val email: String,
    val role: String,
    val home_address: String?,
    val profile_image: String? // Base64 인코딩된 이미지
)

data class ApiResponse<T>(
    val success: Boolean,
    val profile: T? = null,
    val error: String? = null,
    val detail: String? = null
)