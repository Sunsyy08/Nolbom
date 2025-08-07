package com.project.nolbom.data.model

// 서버 응답을 매핑할 데이터 클래스
data class CaptureResponse(
    val success: Boolean,
    val filename: String,
    val path: String,
    val message: String? = null
)
