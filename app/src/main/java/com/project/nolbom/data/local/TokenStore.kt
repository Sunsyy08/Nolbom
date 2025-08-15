// data/local/TokenStore.kt - 기존 코드 + STT 자동 활성화 기능 추가
package com.project.nolbom.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREF = "app_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    // 🔥 STT 상태 저장을 위한 키 추가
    private const val KEY_STT_ACTIVE = "stt_active"
    private const val KEY_STT_AUTO_START = "stt_auto_start"
    private const val KEY_LAST_STT_TIME = "last_stt_time"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    }

    // ========== 기존 토큰 관련 함수들 ==========
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // 🔥 combined_server용 토큰 생성 함수
    fun generateToken(userId: String): String {
        val randomId = (1000..9999).random()
        return "user_${randomId}_${System.currentTimeMillis()}"
    }

    // ========== 사용자 정보 저장 (STT 자동 시작 플래그 포함) ==========
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

            // 🔥 토큰이 없으면 자동 생성
            val finalToken = token ?: generateToken(userId)
            putString(KEY_TOKEN, finalToken)

            // 🔥 회원가입/로그인 시 자동 시작 플래그 설정
            putBoolean(KEY_STT_AUTO_START, true)

            apply()
        }
    }

    // ========== 기존 사용자 정보 함수들 ==========
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

    // ========== 🔥 STT 상태 관리 기능 추가 ==========

    /**
     * STT 활성화 상태 저장
     */
    fun setSTTActive(isActive: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_STT_ACTIVE, isActive)
            if (isActive) {
                putLong(KEY_LAST_STT_TIME, System.currentTimeMillis())
            }
            apply()
        }
    }

    /**
     * STT 활성화 상태 조회
     */
    fun isSTTActive(): Boolean {
        return prefs.getBoolean(KEY_STT_ACTIVE, false)
    }

    /**
     * 자동 시작 여부 설정 (회원가입 후 첫 실행시)
     */
    fun setSTTAutoStart(autoStart: Boolean) {
        prefs.edit()
            .putBoolean(KEY_STT_AUTO_START, autoStart)
            .apply()
    }

    /**
     * 자동 시작 여부 조회
     */
    fun shouldAutoStartSTT(): Boolean {
        return prefs.getBoolean(KEY_STT_AUTO_START, false)
    }

    /**
     * 마지막 STT 활성화 시간
     */
    fun getLastSTTTime(): Long {
        return prefs.getLong(KEY_LAST_STT_TIME, 0L)
    }

    /**
     * STT 관련 모든 상태 초기화
     */
    fun clearSTTStatus() {
        prefs.edit().apply {
            remove(KEY_STT_ACTIVE)
            remove(KEY_STT_AUTO_START)
            remove(KEY_LAST_STT_TIME)
            apply()
        }
    }

    /**
     * 앱이 비정상 종료되었는지 확인 (STT가 활성화되어 있었지만 서비스가 꺼진 상태)
     */
    fun shouldRestoreSTT(): Boolean {
        if (!isLoggedIn()) return false
        if (!isSTTActive()) return false

        val lastTime = getLastSTTTime()
        val currentTime = System.currentTimeMillis()

        // 마지막 활성화가 1시간 이내라면 복원
        return (currentTime - lastTime) < 60 * 60 * 1000
    }

    /**
     * 디버깅용 - 저장된 STT 상태 로그 출력
     */
    fun logSTTStatus() {
        android.util.Log.d("TokenStore", """
            ========== STT 상태 ==========
            사용자 등록: ${isLoggedIn()}
            사용자 이름: ${getUserName()}
            STT 활성화: ${isSTTActive()}
            자동 시작: ${shouldAutoStartSTT()}
            복원 필요: ${shouldRestoreSTT()}
            마지막 시간: ${if (getLastSTTTime() > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(getLastSTTTime())) else "없음"}
            =============================
        """.trimIndent())
    }

    // ========== 기존 삭제 함수들 (STT 상태도 함께 삭제) ==========

    // 전체 데이터 삭제 (로그아웃)
    fun clear() {
        prefs.edit().clear().apply()
    }

    // 토큰만 삭제 (사용자 정보는 유지)
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    // 🔥 사용자 데이터 삭제 (디버깅/테스트용)
    fun clearUserData() {
        prefs.edit().clear().apply()
    }
}