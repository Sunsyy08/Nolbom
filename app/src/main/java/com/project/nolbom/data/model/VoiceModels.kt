// data/model/VoiceModels.kt - 새로 생성
package com.project.nolbom.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 음성 인식 요청
@JsonClass(generateAdapter = true)
data class VoiceRecognitionRequest(
    @Json(name = "audio_data") val audioData: String,  // Base64 인코딩된 음성
    @Json(name = "sample_rate") val sampleRate: Int = 16000,
    @Json(name = "user_id") val userId: Int? = null
)

// 음성 인식 응답
@JsonClass(generateAdapter = true)
data class VoiceRecognitionResponse(
    val success: Boolean,
    val transcript: String,
    @Json(name = "keyword_detected") val keywordDetected: Boolean,
    @Json(name = "sms_sent") val smsSent: Boolean = false,
    val message: String
)

// 연속 음성 인식 응답
@JsonClass(generateAdapter = true)
data class ContinuousVoiceResponse(
    val success: Boolean,
    val message: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "session_id") val sessionId: String
)