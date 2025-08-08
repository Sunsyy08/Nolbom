// repository/UserRepository.kt
package com.project.nolbom.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository(private val context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.75.60:3000/") // 본인 IP로 변경
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // 저장된 토큰 가져오기
    private fun getToken(): String? {
        return sharedPrefs.getString("jwt_token", null)
    }

    // 🆕 회원가입 완료 시 토큰과 사용자 정보 저장
    fun saveTokenAndUserInfo(
        token: String,
        name: String,
        email: String,
        homeAddress: String? = null,
        profileImage: String? = null, // Base64 문자열
        role: String? = null
    ) {
        val editor = sharedPrefs.edit()

        // 토큰 저장
        editor.putString("jwt_token", token)

        // 사용자 정보 저장
        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.putString("user_home_address", homeAddress)
        editor.putString("user_profile_image", profileImage)
        editor.putString("user_role", role)
        editor.putBoolean("has_user_data", true)

        editor.apply()
    }

    // 🆕 회원가입 완료 시 사용자 정보 저장 (Bitmap 버전)
    fun saveUserProfileAfterSignup(
        name: String,
        email: String,
        homeAddress: String? = null,
        profileImageBitmap: Bitmap? = null,
        token: String? = null
    ) {
        val editor = sharedPrefs.edit()

        // 토큰이 있으면 저장
        if (token != null) {
            editor.putString("jwt_token", token)
        }

        // 기본 정보 저장
        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.putString("user_home_address", homeAddress)

        // 프로필 이미지를 Base64로 변환해서 저장
        if (profileImageBitmap != null) {
            val base64Image = bitmapToBase64(profileImageBitmap)
            editor.putString("user_profile_image", base64Image)
        }

        editor.putBoolean("has_user_data", true)
        editor.apply()
    }

    // 🆕 저장된 사용자 정보 불러오기 (API 호출 대신)
    suspend fun getUserProfileFromLocal(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val hasData = sharedPrefs.getBoolean("has_user_data", false)

                if (!hasData) {
                    return@withContext Result.failure(
                        Exception("저장된 사용자 정보가 없습니다.")
                    )
                }

                val userProfile = UserProfile(
                    name = sharedPrefs.getString("user_name", "사용자") ?: "사용자",
                    email = sharedPrefs.getString("user_email", "") ?: "",
                    role = sharedPrefs.getString("user_role", "ward") ?: "ward",
                    home_address = sharedPrefs.getString("user_home_address", null),
                    profile_image = sharedPrefs.getString("user_profile_image", null)
                )

                Result.success(userProfile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 사용자 프로필 정보 가져오기 (API 호출)
    suspend fun getUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext Result.failure(
                    Exception("로그인이 필요합니다.")
                )

                val response = apiService.getUserProfile("Bearer $token")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.profile != null) {
                        Result.success(apiResponse.profile)
                    } else {
                        Result.failure(Exception(apiResponse?.error ?: "프로필 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("네트워크 오류: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Base64 이미지를 Bitmap으로 변환
    fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        return try {
            if (base64String != null && base64String.startsWith("data:image")) {
                // "data:image/jpeg;base64," 부분 제거
                val base64Data = base64String.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Bitmap을 Base64로 변환
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // 🆕 저장된 프로필 이미지를 Bitmap으로 변환
    fun getProfileBitmapFromLocal(): Bitmap? {
        val base64String = sharedPrefs.getString("user_profile_image", null)
        return decodeBase64ToBitmap(base64String)
    }

    // 🆕 저장된 데이터 초기화 (테스트용)
    fun clearUserData() {
        sharedPrefs.edit()
            .clear()
            .apply()
    }

    // ⬇️ 여기부터 새로 추가되는 헬퍼 함수들 ⬇️

    // 🆕 저장된 사용자 이름 가져오기
    fun getStoredUserName(): String? {
        return sharedPrefs.getString("user_name", null)
    }

    // 🆕 저장된 사용자 이메일 가져오기
    fun getStoredUserEmail(): String? {
        return sharedPrefs.getString("user_email", null)
    }

    // 🆕 저장된 사용자 역할 가져오기
    fun getStoredUserRole(): String? {
        return sharedPrefs.getString("user_role", null)
    }

    // 🆕 사용자 데이터 존재 여부 확인
    fun hasUserData(): Boolean {
        return sharedPrefs.getBoolean("has_user_data", false)
    }

    // 🆕 현재 저장된 모든 사용자 정보 로그 출력 (디버깅용)
    fun logStoredUserData() {
        android.util.Log.d("UserRepository", "=== 저장된 사용자 정보 ===")
        android.util.Log.d("UserRepository", "토큰: ${getToken()}")
        android.util.Log.d("UserRepository", "이름: ${getStoredUserName()}")
        android.util.Log.d("UserRepository", "이메일: ${getStoredUserEmail()}")
        android.util.Log.d("UserRepository", "주소: ${sharedPrefs.getString("user_home_address", null)}")
        android.util.Log.d("UserRepository", "역할: ${getStoredUserRole()}")
        android.util.Log.d("UserRepository", "데이터 존재: ${hasUserData()}")
        android.util.Log.d("UserRepository", "========================")
    }

    // 🆕 테스트용 더미 데이터 저장
    fun saveTestData() {
        saveTokenAndUserInfo(
            token = "test_token_12345",
            name = "테스트 사용자",
            email = "test@example.com",
            homeAddress = "서울시 강남구 테스트동 123번지",
            profileImage = null,
            role = "ward"
        )
        android.util.Log.d("UserRepository", "테스트 데이터 저장 완료")
    }
}