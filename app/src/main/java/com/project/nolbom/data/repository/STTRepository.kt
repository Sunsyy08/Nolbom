// data/repository/STTRepository.kt - null 안전성 수정 버전
package com.project.nolbom.data.repository

import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.model.*
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class STTRepository {

    /**
     * STT 활성화/비활성화
     */
    // STTRepository.kt - activateSTT 함수만 수정
    suspend fun activateSTT(enable: Boolean): Result<STTActivationResponse> {
        return try {
            val token = TokenStore.getToken()
            println("🔍 STT 활성화 시도 - 토큰: ${token?.take(20)}...")

            if (token.isNullOrEmpty()) {
                println("❌ 토큰이 없음")
                return Result.failure(Exception("토큰이 없습니다. 다시 로그인해주세요."))
            }

            val request = STTActivationRequest(enable = enable)
            println("🔍 STT API 호출 시작 - enable: $enable")

            val response = RetrofitClient.sttApi.activateSTT("Bearer $token", request)
            println("🔍 STT API 응답 - 코드: ${response.code()}, 성공: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                println("✅ STT 활성화 성공 - 응답: $body")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ STT 활성화 실패 - 에러: $errorBody")
                Result.failure(Exception("STT 활성화 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            println("❌ STT 활성화 예외 - ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * STT 상태 확인
     */
    suspend fun getSTTStatus(): Result<STTStatusResponse> {
        return try {
            val token = TokenStore.getToken()
            if (token.isNullOrEmpty()) {  // 🔧 null 안전성 추가
                return Result.failure(Exception("토큰이 없습니다."))
            }

            val response = RetrofitClient.sttApi.getSTTStatus("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("STT 상태 조회 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 서버 상태 확인
     */
    suspend fun getServerStatus(): Result<ServerStatusResponse> {
        return try {
            val response = RetrofitClient.sttApi.getServerStatus()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("서버 상태 조회 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 감지 기록 조회
     */
    suspend fun getDetections(limit: Int = 10): Result<DetectionsResponse> {
        return try {
            val response = RetrofitClient.sttApi.getDetections(limit)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("감지 기록 조회 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 수동 긴급 SMS 전송
     */
    suspend fun sendEmergencySMS(): Result<SMSResponse> {
        return try {
            val response = RetrofitClient.sttApi.sendEmergencySMS()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("긴급 SMS 전송 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 모니터링 상태 실시간 조회 (Flow)
     */
    fun getMonitoringStatusFlow(): Flow<Result<MonitoringStatusResponse>> = flow {
        while (true) {
            try {
                val response = RetrofitClient.sttApi.getMonitoringStatus()

                if (response.isSuccessful && response.body() != null) {
                    emit(Result.success(response.body()!!))
                } else {
                    emit(Result.failure(Exception("모니터링 상태 조회 실패")))
                }

                kotlinx.coroutines.delay(5000) // 5초마다 업데이트
            } catch (e: Exception) {
                emit(Result.failure(e))
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    /**
     * 활성 사용자 목록 조회
     */
    suspend fun getActiveUsers(): Result<ActiveUsersResponse> {
        return try {
            val response = RetrofitClient.sttApi.getActiveUsers()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("활성 사용자 조회 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 음성 데이터를 서버로 전송하여 인식
     */
    suspend fun recognizeVoice(audioBase64: String): Result<VoiceRecognitionResponse> {
        return try {
            val token = TokenStore.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("토큰이 없습니다."))
            }

            val request = VoiceRecognitionRequest(
                audioData = audioBase64,
                sampleRate = 16000
            )

            println("🔍 음성 데이터 전송 중... (${audioBase64.length} 문자)")

            val response = RetrofitClient.sttApi.recognizeVoice("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                println("✅ 음성 인식 성공: ${result.transcript}")
                if (result.keywordDetected) {
                    println("🚨 키워드 감지됨!")
                }
                Result.success(result)
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ 음성 인식 실패: $errorBody")
                Result.failure(Exception("음성 인식 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            println("❌ 음성 인식 예외: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 연속 음성 인식 시작
     */
    suspend fun startContinuousVoice(): Result<ContinuousVoiceResponse> {
        return try {
            val token = TokenStore.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("토큰이 없습니다."))
            }

            val response = RetrofitClient.sttApi.startContinuousRecognition("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("연속 음성 인식 시작 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}