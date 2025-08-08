// data/repository/ProfileRepository.kt
package com.project.nolbom.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.project.nolbom.data.model.ProfileUserData
import com.project.nolbom.data.model.ProfileResponse
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val context: Context) {
    private val apiService = RetrofitClient.api
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("eldercare_prefs", Context.MODE_PRIVATE)

    private fun getAuthToken(): String? {
        // 로그인 토큰이 있으면 사용, 없으면 회원가입 토큰 사용
        return sharedPrefs.getString("auth_token", null)
            ?: sharedPrefs.getString("signup_token", null)
            ?: sharedPrefs.getString("token", null) // 다른 키로 저장된 경우도 고려
    }

    suspend fun getProfile(): Result<ProfileUserData> = withContext(Dispatchers.IO) {
        try {
            val token = getAuthToken() ?: return@withContext Result.failure(
                Exception("토큰이 없습니다. 회원가입 또는 로그인이 필요합니다.")
            )

            val response = apiService.getProfile("Bearer $token")

            // 📌 여기서 서버 응답 원문 확인
            Log.d("ProfileRepository", "Response raw: ${response.raw()}")
            Log.d("ProfileRepository", "Response body: ${response.body()}")
            Log.d("ProfileRepository", "Response errorBody: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse?.success == true && profileResponse.profile != null) {
                    Result.success(profileResponse.profile)
                } else {
                    Result.failure(Exception(profileResponse?.error ?: "프로필 조회 실패"))
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

    // 토큰이 있는지 확인하는 메서드
    fun hasValidToken(): Boolean {
        return getAuthToken() != null
    }

    // 토큰을 저장하는 메서드 (회원가입/로그인 시 사용)
    fun saveToken(token: String) {
        sharedPrefs.edit()
            .putString("auth_token", token)
            .apply()
    }
}