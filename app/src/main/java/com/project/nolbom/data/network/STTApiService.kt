// data/network/STTApiService.kt - 새로 추가
package com.project.nolbom.data.network

import com.project.nolbom.data.model.ActiveUsersResponse
import com.project.nolbom.data.model.ContinuousVoiceResponse
import com.project.nolbom.data.model.DetectionsResponse
import com.project.nolbom.data.model.EmergencyResponse
import com.project.nolbom.data.model.KeywordsResponse
import com.project.nolbom.data.model.MonitoringStatusResponse
import com.project.nolbom.data.model.SMSTestResponse
import com.project.nolbom.data.model.STTActivationRequest
import com.project.nolbom.data.model.STTActivationResponse
import com.project.nolbom.data.model.STTStatusResponse
import com.project.nolbom.data.model.ServerStatusResponse
import com.project.nolbom.data.model.VoiceRecognitionRequest
import com.project.nolbom.data.model.VoiceRecognitionResponse
import retrofit2.Response
import retrofit2.http.*

interface STTApiService {

    // 🔥 combined_server.py의 정확한 엔드포인트: /stt/activate
    @POST("stt/activate")
    suspend fun activateSTT(
        @Header("Authorization") token: String,
        @Body request: STTActivationRequest
    ): Response<STTActivationResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /stt/status
    @GET("stt/status")
    suspend fun getSTTStatus(
        @Header("Authorization") token: String
    ): Response<STTStatusResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /health
    @GET("health")
    suspend fun getServerStatus(): Response<ServerStatusResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /detections
    @GET("detections")
    suspend fun getDetections(
        @Query("limit") limit: Int = 10
    ): Response<DetectionsResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /emergency/manual
    @POST("emergency/manual")
    suspend fun sendEmergencySMS(
        @Header("Authorization") token: String
    ): Response<EmergencyResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /voice/recognize
    @POST("voice/recognize")
    suspend fun recognizeVoice(
        @Header("Authorization") token: String,
        @Body request: VoiceRecognitionRequest
    ): Response<VoiceRecognitionResponse>

    // 연속 음성 인식 (향후 combined_server에 추가 예정)
    @POST("voice/continuous")
    suspend fun startContinuousRecognition(
        @Header("Authorization") token: String
    ): Response<ContinuousVoiceResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /sms/test
    @POST("sms/test")
    suspend fun testSMS(
        @Query("message") message: String = "테스트 메시지입니다."
    ): Response<SMSTestResponse>

    // 🔥 combined_server.py의 정확한 엔드포인트: /config/keywords
    @GET("config/keywords")
    suspend fun getKeywords(): Response<KeywordsResponse>

    // 모니터링 상태는 combined_server에 없으므로 임시로 health로 대체
    @GET("health")
    suspend fun getMonitoringStatus(): Response<MonitoringStatusResponse>
}