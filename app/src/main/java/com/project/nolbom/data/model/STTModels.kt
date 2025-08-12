// data/model/STTModels.kt - 새로 추가
package com.project.nolbom.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// STT 활성화 요청
@JsonClass(generateAdapter = true)
data class STTActivationRequest(
    val enable: Boolean = true
)

// STT 활성화 응답
@JsonClass(generateAdapter = true)
data class STTActivationResponse(
    val success: Boolean,
    val message: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "activated_at") val activatedAt: String? = null
)

// STT 상태 응답
@JsonClass(generateAdapter = true)
data class STTStatusResponse(
    val success: Boolean,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "stt_active") val sttActive: Boolean,
    @Json(name = "session_info") val sessionInfo: UserSession? = null,
    @Json(name = "total_active_users") val totalActiveUsers: Int
)

// 사용자 세션 정보
@JsonClass(generateAdapter = true)
data class UserSession(
    val token: String,
    val name: String,
    val role: String,
    @Json(name = "activated_at") val activatedAt: String
)

// 서버 상태 응답
@JsonClass(generateAdapter = true)
data class ServerStatusResponse(
    @Json(name = "server_running") val serverRunning: Boolean,
    @Json(name = "monitoring_active") val monitoringActive: Boolean,
    @Json(name = "active_users_count") val activeUsersCount: Int,
    @Json(name = "total_detections") val totalDetections: Int,
    @Json(name = "last_detection") val lastDetection: String? = null,
    @Json(name = "system_health") val systemHealth: SystemHealth? = null
)

// 시스템 상태
@JsonClass(generateAdapter = true)
data class SystemHealth(
    @Json(name = "cpu_percent") val cpuPercent: Double,
    @Json(name = "memory_percent") val memoryPercent: Double,
    @Json(name = "microphone_ok") val microphoneOk: Boolean? = null
)

// 감지 기록 응답
@JsonClass(generateAdapter = true)
data class DetectionsResponse(
    val total: Int,
    val detections: List<Detection>
)

// 감지 기록
@JsonClass(generateAdapter = true)
data class Detection(
    val keyword: String,
    val timestamp: String,
    val transcript: String,
    @Json(name = "sms_sent") val smsSent: Boolean,
    @Json(name = "message_id") val messageId: String? = null,
    @Json(name = "active_users") val activeUsers: List<Int>? = null
)

// SMS 응답
@JsonClass(generateAdapter = true)
data class SMSResponse(
    val status: String,
    val response: Map<String, Any>,
    val timestamp: String
)

// 모니터링 상태 응답
@JsonClass(generateAdapter = true)
data class MonitoringStatusResponse(
    val active: Boolean,
    @Json(name = "detected_count") val detectedCount: Int,
    @Json(name = "last_detection") val lastDetection: String? = null,
    @Json(name = "cooldown_seconds") val cooldownSeconds: Int,
    @Json(name = "active_users_count") val activeUsersCount: Int,
    @Json(name = "system_health") val systemHealth: SystemHealth? = null
)

// 활성 사용자 응답
@JsonClass(generateAdapter = true)
data class ActiveUsersResponse(
    val success: Boolean,
    @Json(name = "active_users") val activeUsers: Map<String, UserSession>,
    val count: Int
)