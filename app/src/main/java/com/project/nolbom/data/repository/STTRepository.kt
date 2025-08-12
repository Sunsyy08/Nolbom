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
    suspend fun activateSTT(enable: Boolean): Result<STTActivationResponse> {
        return try {
            val token = TokenStore.getToken()
            if (token.isNullOrEmpty()) {  // 🔧 null 안전성 추가
                return Result.failure(Exception("토큰이 없습니다. 다시 로그인해주세요."))
            }

            val request = STTActivationRequest(enable = enable)
            val response = RetrofitClient.sttApi.activateSTT("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("STT 활성화 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
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
}