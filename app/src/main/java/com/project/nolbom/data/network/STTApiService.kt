// data/network/STTApiService.kt - 새로 추가
package com.project.nolbom.data.network

import com.project.nolbom.data.model.ActiveUsersResponse
import com.project.nolbom.data.model.ContinuousVoiceResponse
import com.project.nolbom.data.model.DetectionsResponse
import com.project.nolbom.data.model.MonitoringStatusResponse
import com.project.nolbom.data.model.SMSResponse
import com.project.nolbom.data.model.STTActivationRequest
import com.project.nolbom.data.model.STTActivationResponse
import com.project.nolbom.data.model.STTStatusResponse
import com.project.nolbom.data.model.ServerStatusResponse
import com.project.nolbom.data.model.VoiceRecognitionRequest
import com.project.nolbom.data.model.VoiceRecognitionResponse
import retrofit2.Response
import retrofit2.http.*

interface STTApiService {
    // STT 활성화/비활성화
    @POST("auth/activate")
    suspend fun activateSTT(
        @Header("Authorization") token: String,
        @Body request: STTActivationRequest
    ): Response<STTActivationResponse>

    // STT 상태 확인
    @GET("auth/status")
    suspend fun getSTTStatus(
        @Header("Authorization") token: String
    ): Response<STTStatusResponse>

    // 서버 상태 확인
    @GET("status")
    suspend fun getServerStatus(): Response<ServerStatusResponse>

    // 감지 기록 조회
    @GET("detections")
    suspend fun getDetections(
        @Query("limit") limit: Int = 10
    ): Response<DetectionsResponse>

    // 수동 긴급 SMS 전송
    @POST("sms/emergency")
    suspend fun sendEmergencySMS(): Response<SMSResponse>

    // 모니터링 상태 확인 (토큰 불필요)
    @GET("monitoring/status")
    suspend fun getMonitoringStatus(): Response<MonitoringStatusResponse>

    // 활성화된 사용자 목록 (관리자용)
    @GET("users/active")
    suspend fun getActiveUsers(): Response<ActiveUsersResponse>

    // 음성 인식 API
    @POST("voice/recognize")
    suspend fun recognizeVoice(
        @Header("Authorization") token: String,
        @Body request: VoiceRecognitionRequest
    ): Response<VoiceRecognitionResponse>

    // 연속 음성 인식 시작
    @POST("voice/continuous")
    suspend fun startContinuousRecognition(
        @Header("Authorization") token: String
    ): Response<ContinuousVoiceResponse>
}