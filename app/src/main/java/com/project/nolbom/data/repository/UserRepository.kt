package com.project.nolbom.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val context: Context) {

    private val apiService = RetrofitClient.api
    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getToken(): String? = TokenStore.getToken()

    fun saveTokenAndUserInfo(
        token: String,
        name: String,
        email: String,
        homeAddress: String? = null,
        profileImage: String? = null,
        role: String? = null
    ) {
        TokenStore.saveToken(token) // ✅ 인터셉터가 읽는 저장소

        val editor = sharedPrefs.edit()
        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.putString("user_home_address", homeAddress)
        editor.putString("user_profile_image", profileImage)
        editor.putString("user_role", role)
        editor.putBoolean("has_user_data", true)
        editor.apply()
    }

    suspend fun getUserProfileFromLocal(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val hasData = sharedPrefs.getBoolean("has_user_data", false)
            if (!hasData) return@withContext Result.failure(Exception("저장된 사용자 정보가 없습니다."))

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

    suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            if (getToken().isNullOrBlank()) {
                return@withContext Result.failure(Exception("로그인이 필요합니다."))
            }
            val response = apiService.getUserProfile() // ✅ 인자 없이

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

    fun decodeBase64ToBitmap(base64String: String?): Bitmap? = try {
        if (base64String != null && base64String.startsWith("data:image")) {
            val base64Data = base64String.substringAfter("base64,")
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } else null
    } catch (_: Exception) { null }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val os = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os)
        val byteArray = os.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun getProfileBitmapFromLocal(): Bitmap? =
        decodeBase64ToBitmap(sharedPrefs.getString("user_profile_image", null))

    fun clearUserData() {
        sharedPrefs.edit().clear().apply()
    }

    fun getStoredUserName(): String? = sharedPrefs.getString("user_name", null)
    fun getStoredUserEmail(): String? = sharedPrefs.getString("user_email", null)
    fun getStoredUserRole(): String? = sharedPrefs.getString("user_role", null)
    fun hasUserData(): Boolean = sharedPrefs.getBoolean("has_user_data", false)

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
