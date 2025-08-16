// MissingPerson.kt
package com.project.nolbom.data.model

// 실종자 정보 데이터 클래스
data class MissingPerson(
    val id: Int,
    val ward_id: Int,
    val name: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val gender: String?,
    val phone: String?,
    val home_address: String?,
    val medical_status: String?,
    val detected_at: String,
    val status: String,
    val current_lat: Double?,
    val current_lng: Double?,
    val profile_image: String?,
    val updated_at: String,
    val notes: String?
)

// 🔧 기존 ApiResponse를 재사용하되, 실종자용 특화 응답 클래스 추가
data class MissingPersonResponse(
    val success: Boolean,
    val data: MissingPerson? = null,
    val error: String? = null,
    val detail: String? = null
)

// 실종자 목록 응답
data class MissingPersonsResponse(
    val success: Boolean,
    val data: List<MissingPerson> = emptyList(),
    val total: Int = 0,
    val count: Int = 0,
    val error: String? = null
)

// 요청 데이터 클래스들
data class FoundRequest(
    val found_lat: Double?,
    val found_lng: Double?,
    val notes: String?
)

data class LocationUpdateRequest(
    val lat: Double,
    val lng: Double
)