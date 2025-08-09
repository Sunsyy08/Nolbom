package com.project.nolbom.data.repository

import android.content.Context
import android.util.Log
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.model.ProfileUserData
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val context: Context) {
    private val apiService = RetrofitClient.api

    private fun getAuthToken(): String? = TokenStore.getToken()  // ✅ TokenStore로 통일

    suspend fun getProfile(): Result<ProfileUserData> = withContext(Dispatchers.IO) {
        try {
            // 인터셉터가 헤더를 붙이지만, 사용자에게 친절한 메시지를 위해 존재 여부만 확인
            if (getAuthToken().isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("토큰이 없습니다. 회원가입 또는 로그인이 필요합니다.")
                )
            }

            // 이 코드 수정, 추가 하니깐 갑자기 됨.
            val token = TokenStore.getToken() ?: return@withContext Result.failure(Exception("토큰 없음"))
            val response = apiService.getProfile("Bearer $token")


            Log.d("ProfileRepository", "Response raw: ${response.raw()}")
            Log.d("ProfileRepository", "Response body: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.profile != null) {
                    Result.success(body.profile)
                } else {
                    Result.failure(Exception(body?.error ?: "프로필 조회 실패"))
                }
            } else {
                when (response.code()) {
                    401 -> Result.failure(Exception("인증이 만료되었습니다. 다시 로그인해주세요."))
                    404 -> Result.failure(Exception("사용자 정보를 찾을 수 없습니다."))
                    else -> Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hasValidToken(): Boolean = !getAuthToken().isNullOrBlank()

    fun saveToken(token: String) {
        TokenStore.saveToken(token)  // ✅ 저장도 TokenStore로 통일
    }
}
