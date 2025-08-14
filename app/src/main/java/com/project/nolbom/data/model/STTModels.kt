// data/model/STTModels.kt - 새로 추가
package com.project.nolbom.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// STT 활성화 요청
@JsonClass(generateAdapter = true)
data class STTActivationRequest(
    val enable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class STTActivationResponse(
    val success: Boolean,
    val message: String,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "activated_at") val activatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class STTStatusResponse(
    val success: Boolean,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "stt_active") val sttActive: Boolean,
    @Json(name = "session_info") val sessionInfo: Map<String, Any>? = null,
    @Json(name = "total_active_users") val totalActiveUsers: Int
)

@JsonClass(generateAdapter = true)
data class ServerStatusResponse(
    val status: String,
    val service: String,
    @Json(name = "active_users") val activeUsersCount: Int,
    val features: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class VoiceRecognitionRequest(
    @Json(name = "audio_data") val audioData: String,
    @Json(name = "sample_rate") val sampleRate: Int = 16000,
    @Json(name = "user_id") val userId: Int? = null
)

@JsonClass(generateAdapter = true)
data class VoiceRecognitionResponse(
    val success: Boolean,
    val transcript: String,
    @Json(name = "keyword_detected") val keywordDetected: Boolean,
    @Json(name = "sms_sent") val smsSent: Boolean = false,
    val message: String
)

@JsonClass(generateAdapter = true)
data class EmergencyResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class DetectionsResponse(
    val total: Int,
    val detections: List<Detection>
)

@JsonClass(generateAdapter = true)
data class Detection(
    val keyword: String,
    val timestamp: String,
    val transcript: String,
    val source: String? = null,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "sms_sent") val smsSent: Boolean,
    @Json(name = "message_id") val messageId: String? = null,
    @Json(name = "sms_response") val smsResponse: Map<String, Any>? = null,
    val confidence: Double? = null,
    @Json(name = "audio_info") val audioInfo: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class SMSTestResponse(
    val status: String,
    val response: Map<String, Any>,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class KeywordsResponse(
    val keywords: List<String>,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class ContinuousVoiceResponse(
    val success: Boolean,
    val message: String,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "session_id") val sessionId: String? = null
)

// 🔧 typealias 완전히 제거하고 별도 타입으로 정의
typealias MonitoringStatusResponse = STTStatusResponse
typealias ActiveUsersResponse = STTStatusResponse