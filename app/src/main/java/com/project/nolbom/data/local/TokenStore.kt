package com.project.nolbom.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREF = "app_prefs"
    private const val KEY_TOKEN = "jwt_token"

    // 🔥 사용자 정보 키들 추가
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    }

    // 기존 토큰 관련 함수들
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // 🔥 사용자 정보 저장 (회원가입/로그인 시 호출)
    fun saveUserInfo(
        userId: String,
        userName: String,
        userPhone: String? = null,
        userEmail: String? = null,
        token: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_PHONE, userPhone)
            putString(KEY_USER_EMAIL, userEmail)
            putBoolean(KEY_IS_LOGGED_IN, true)
            token?.let { putString(KEY_TOKEN, it) }
            apply()
        }
    }

    // 사용자 정보 가져오기
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // 🔥 위치 추적용 고유 ID 생성 (한 번만 생성)
    fun getOrCreateLocationUserId(): String {
        val existingId = getUserId()
        if (existingId != null) return existingId

        // 새로운 고유 ID 생성
        val userName = getUserName() ?: "user"
        val timestamp = System.currentTimeMillis()
        val randomNum = (1000..9999).random()
        val newId = "${userName}_${timestamp}_$randomNum"

        // 생성된 ID 저장
        prefs.edit().putString(KEY_USER_ID, newId).apply()
        return newId
    }

    // 전체 데이터 삭제 (로그아웃)
    fun clear() {
        prefs.edit().clear().apply()
    }

    // 토큰만 삭제 (사용자 정보는 유지)
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
}