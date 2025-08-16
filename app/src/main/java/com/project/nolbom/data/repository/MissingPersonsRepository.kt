// MissingPersonsRepository.kt
package com.project.nolbom.data.repository

import com.project.nolbom.data.model.FoundRequest
import com.project.nolbom.data.model.LocationUpdateRequest
import com.project.nolbom.data.model.MissingPerson
import com.project.nolbom.data.model.MissingPersonsResponse
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MissingPersonsRepository {

    private val api = RetrofitClient.missingPersonsApi

    // 실종자 목록 조회
    suspend fun getMissingPersons(
        status: String = "MISSING",
        limit: Int = 50,
        offset: Int = 0
    ): Result<MissingPersonsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMissingPersons(status, limit, offset)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API 호출 실패: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 특정 실종자 상세 조회 - 🔧 기존 ApiResponse 구조에 맞게 수정
    suspend fun getMissingPersonDetail(id: Int): Result<MissingPerson> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMissingPersonDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val person = response.body()!!.profile // 🔧 data 대신 profile 사용
                if (person != null) {
                    Result.success(person)
                } else {
                    Result.failure(Exception("실종자 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.error ?: "실종자 정보 조회 실패"
                Result.failure(Exception("$errorMsg: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 실종자 발견 처리 - 🔧 기존 ApiResponse 구조에 맞게 수정
    suspend fun markAsFound(
        id: Int,
        lat: Double? = null,
        lng: Double? = null,
        notes: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = FoundRequest(lat, lng, notes)
            val response = api.markAsFound(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()!!.profile ?: "발견 처리가 완료되었습니다" // 🔧 profile 필드 사용
                Result.success(message.toString())
            } else {
                val errorMsg = response.body()?.error ?: "발견 처리 실패"
                Result.failure(Exception("$errorMsg: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 위치 업데이트 - 🔧 기존 ApiResponse 구조에 맞게 수정
    suspend fun updateLocation(
        id: Int,
        lat: Double,
        lng: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = LocationUpdateRequest(lat, lng)
            val response = api.updateLocation(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success("위치가 업데이트되었습니다")
            } else {
                val errorMsg = response.body()?.error ?: "위치 업데이트 실패"
                Result.failure(Exception("$errorMsg: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 연결 테스트 - 🔧 RetrofitClient 사용
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 🔧 RetrofitClient의 테스트 함수 사용
            val isConnected = RetrofitClient.testMissingPersonsConnection()
            Result.success(isConnected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}