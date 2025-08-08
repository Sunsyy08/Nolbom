package com.project.nolbom.data.model

data class GenericResponse(
    val success: Boolean,
    val message: String?,
    val token: String? = null,      // 🆕 추가
    val role: String? = null,       // 🆕 추가
    val error: String? = null
)

