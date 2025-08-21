/**
 * 파일명: STTApiService.kt
 * 위치: data/network/
 *
 * 설명:
 *  - Retrofit 인터페이스 정의 파일
 *  - Combined Server (Python 기반 STT 서버)와의 HTTP 통신 관리
 *  - STT 활성화, 상태 조회, 음성 인식, 긴급 알림, 키워드 조회 등
 *
 * 주요 엔드포인트:
 *  - POST /stt/activate → STT 활성화 요청
 *  - GET  /stt/status   → STT 상태 조회
 *  - GET  /health       → 서버 상태 조회
 *  - GET  /detections   → 감지 이벤트 조회
 *  - POST /emergency/manual → 긴급 SMS 전송
 *  - POST /voice/recognize  → 음성 인식
 *  - POST /voice/continuous → 연속 음성 인식
 *  - POST /sms/test        → 테스트 SMS 전송
 *  - GET  /config/keywords → 키워드 목록 조회
 *  - GET  /health           → 모니터링 상태 조회 (임시)
 *
 * 주의:
 *  - 모든 요청은 Authorization 헤더 (JWT 토큰) 필요
 *  - 일부 엔드포인트는 Combined Server 상황에 따라 변경될 수 있음
 *  - 연속 음성 인식 및 모니터링 상태는 향후 서버 기능 확장에 따라 수정 예정
 */

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