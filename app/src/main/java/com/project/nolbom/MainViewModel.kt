// MainViewModel.kt - 기존 코드에 STT 기능 추가
package com.project.nolbom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.repository.UserRepository
import com.project.nolbom.data.repository.STTRepository
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.utils.RealtimeVoiceService
import com.project.nolbom.utils.VoiceRecorder
import kotlinx.coroutines.delay

// 🔥 UI 상태 클래스에 STT 관련 필드 추가
data class MainUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val profileBitmap: Bitmap? = null,
    val error: String? = null,

    // 🔥 STT 관련 상태 추가
    val isRecording: Boolean = false,
    val userRegistered: Boolean = false,
    val serverConnected: Boolean = false,
    val isSTTActive: Boolean = false,
    val userId: String? = null,
    val userName: String = "",
    val userPhone: String = "",
    val lastTranscript: String = "",
    val keywordDetected: Boolean = false,
    val smsSent: Boolean = false
)

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // 🔥 STT 관련 추가
    private val sttRepository = STTRepository()
    private var voiceRecorder: VoiceRecorder? = null

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    init {
        loadUserProfile()
        loadSTTUserInfo() // 🔥 STT 사용자 정보도 로드
    }

    // ========== 🔥 기존 프로필 관련 코드 (그대로 유지) ==========
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            android.util.Log.d("MainViewModel", "프로필 로딩 시작")

            // 🆕 먼저 로컬 데이터 시도, 실패하면 API 호출
            userRepository.getUserProfileFromLocal()
                .onSuccess { profile ->
                    android.util.Log.d("MainViewModel", "로컬 데이터 로드 성공: ${profile.name}")
                    val bitmap = userRepository.getProfileBitmapFromLocal()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = profile,
                        profileBitmap = bitmap
                    )
                }
                .onFailure { localError ->
                    android.util.Log.d("MainViewModel", "로컬 데이터 실패: ${localError.message}")
                    // 로컬 데이터가 없으면 API 호출 시도
                    userRepository.getUserProfile()
                        .onSuccess { profile ->
                            android.util.Log.d("MainViewModel", "API 데이터 로드 성공: ${profile.name}")
                            val bitmap = userRepository.decodeBase64ToBitmap(profile.profile_image)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                userProfile = profile,
                                profileBitmap = bitmap
                            )
                        }
                        .onFailure { apiError ->
                            android.util.Log.d("MainViewModel", "API 데이터 실패: ${apiError.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = apiError.message
                            )
                        }
                }
        }
    }

    fun retryLoadProfile() {
        loadUserProfile()
    }

    // 🆕 데이터 초기화 (테스트용)
    fun clearUserData() {
        userRepository.clearUserData()
        _uiState.value = MainUiState()
        loadUserProfile()
    }

    // 🆕 테스트 데이터 저장 (테스트용)
    fun saveTestData() {
        userRepository.saveTestData()
        loadUserProfile()
    }

    // 🆕 저장된 데이터 로그 출력 (디버깅용)
    fun logUserData() {
        userRepository.logStoredUserData()
    }

    // ========== 🔥 새로 추가된 STT 관련 코드 ==========

    fun initVoiceRecorder(context: Context) {
        voiceRecorder = VoiceRecorder(context)
    }

    private fun loadSTTUserInfo() {
        val userId = TokenStore.getUserId()
        val userName = TokenStore.getUserName()
        val userPhone = TokenStore.getUserPhone()
        val isRegistered = TokenStore.isLoggedIn()

        _uiState.value = _uiState.value.copy(
            userRegistered = isRegistered,
            userId = userId,
            userName = userName ?: "",
            userPhone = userPhone ?: ""
        )

        if (isRegistered) {
            addMessage("✅ STT 사용자 정보 로드됨: $userName")
        }
    }

    fun isUserRegistered(): Boolean = TokenStore.isLoggedIn()

    // 🔥 회원가입 후 자동으로 STT 켜기
    fun registerUser(userName: String, userPhone: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("📝 회원가입 중...")

        viewModelScope.launch {
            try {
                val userId = TokenStore.getOrCreateLocationUserId()
                val token = TokenStore.generateToken(userId)

                TokenStore.saveUserInfo(
                    userId = userId,
                    userName = userName,
                    userPhone = userPhone,
                    token = token
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userRegistered = true,
                    userId = userId,
                    userName = userName,
                    userPhone = userPhone
                )

                addMessage("✅ 회원가입 완료: $userName")

                // 🔥 자동으로 서버 연결 후 STT 켜기
                checkServerHealth()
                delay(1000) // 서버 연결 확인 후
                activateSTTAndStartService()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ 회원가입 실패: ${e.message}")
                Log.e("MainViewModel", "회원가입 실패", e)
            }
        }
    }

    // 🔥 STT 활성화 + 실시간 서비스 시작
    fun activateSTTAndStartService() {
        if (!_uiState.value.userRegistered) {
            addMessage("❌ 먼저 회원가입이 필요합니다")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🔄 STT 활성화 및 실시간 감지 시작 중...")

        viewModelScope.launch {
            try {
                val result = sttRepository.activateSTT(enable = true)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSTTActive = true
                            )

                            addMessage("✅ STT 활성화 성공!")

                            // 🔥 실시간 음성 감지 서비스 시작
                            currentContext?.let { context ->
                                startRealtimeVoiceService(context)
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            addMessage("❌ STT 활성화 실패: ${response.message}")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        addMessage("❌ STT 서버 연결 실패: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ STT 활성화 오류: ${e.message}")
            }
        }
    }

    // 🔥 STT 비활성화 (완전 중지)
    fun deactivateSTT() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🛑 STT 비활성화 중...")

        viewModelScope.launch {
            try {
                // 서비스 중지
                currentContext?.let { context ->
                    stopRealtimeVoiceService(context)
                }

                // 서버에 비활성화 요청
                val result = sttRepository.activateSTT(enable = false)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSTTActive = false
                )

                addMessage("✅ STT 완전히 비활성화됨")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ STT 비활성화 오류: ${e.message}")
            }
        }
    }

    // 🔥 실시간 음성 감지 서비스 제어
    private var currentContext: Context? = null

    fun setContext(context: Context) {
        currentContext = context
    }

    fun startRealtimeVoiceService(context: Context) {
        val intent = Intent(context, RealtimeVoiceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        addMessage("🎤 실시간 음성 감지 시작됨 - 화면이 꺼져도 계속 작동합니다")
    }

    fun stopRealtimeVoiceService(context: Context) {
        val intent = Intent(context, RealtimeVoiceService::class.java)
        context.stopService(intent)
        addMessage("🛑 실시간 음성 감지 중지됨")
    }

    // 🔥 앱 시작 시 기존 사용자 자동 활성화
    fun activateSTTIfNeeded() {
        if (!_uiState.value.isSTTActive && _uiState.value.userRegistered) {
            activateSTTAndStartService()
        }
    }


//    fun activateSTTIfNeeded() {
//        if (!_uiState.value.isSTTActive && _uiState.value.userRegistered) {
//            activateSTT()
//        }
//    }

    fun activateSTT() {
        if (!_uiState.value.userRegistered) {
            addMessage("❌ 먼저 회원가입이 필요합니다")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🔄 combined_server STT 활성화 중...")

        viewModelScope.launch {
            try {
                val result = sttRepository.activateSTT(enable = true)

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSTTActive = response.success
                        )
                        if (response.success) {
                            addMessage("✅ combined_server STT 활성화 성공: ${response.message}")
                        } else {
                            addMessage("❌ STT 활성화 실패: ${response.message}")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        addMessage("❌ combined_server 연결 실패: ${error.message}")
                        Log.e("MainViewModel", "STT 활성화 실패", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ STT 활성화 오류: ${e.message}")
                Log.e("MainViewModel", "STT 활성화 오류", e)
            }
        }
    }

    fun checkServerHealth() {
        viewModelScope.launch {
            try {
                val result = sttRepository.getServerStatus()

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(serverConnected = true)
                        addMessage("✅ combined_server 연결 성공 (활성 사용자: ${response.activeUsersCount}명)")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(serverConnected = false)
                        addMessage("❌ combined_server 연결 실패: ${error.message}")
                        Log.e("MainViewModel", "서버 연결 실패", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(serverConnected = false)
                addMessage("❌ combined_server 확인 오류: ${e.message}")
                Log.e("MainViewModel", "서버 확인 오류", e)
            }
        }
    }

    fun testMicrophone() {
        if (voiceRecorder == null) {
            addMessage("❌ VoiceRecorder가 초기화되지 않았습니다")
            return
        }

        addMessage("🧪 마이크 테스트 중...")

        viewModelScope.launch {
            try {
                val result = voiceRecorder!!.testMicrophone()
                addMessage("🧪 마이크 테스트 결과: $result")
            } catch (e: Exception) {
                addMessage("❌ 마이크 테스트 실패: ${e.message}")
                Log.e("MainViewModel", "마이크 테스트 실패", e)
            }
        }
    }

    fun startVoiceRecognition() {
        if (!_uiState.value.isSTTActive) {
            addMessage("❌ 먼저 STT를 활성화해주세요")
            return
        }

        if (voiceRecorder == null) {
            addMessage("❌ VoiceRecorder가 초기화되지 않았습니다")
            return
        }

        _uiState.value = _uiState.value.copy(isRecording = true)
        addMessage("🎤 음성 인식 시작...")

        viewModelScope.launch {
            try {
                val audioBase64 = voiceRecorder!!.recordShortAudio(3000)

                if (audioBase64 != null) {
                    addMessage("📡 combined_server로 음성 데이터 전송 중...")

                    val result = sttRepository.recognizeVoice(audioBase64)

                    result.fold(
                        onSuccess = { response ->
                            _uiState.value = _uiState.value.copy(
                                isRecording = false,
                                lastTranscript = response.transcript,
                                keywordDetected = response.keywordDetected,
                                smsSent = response.smsSent
                            )

                            if (response.transcript.isNotEmpty()) {
                                addMessage("✅ 인식 결과: ${response.transcript}")
                            }

                            if (response.keywordDetected) {
                                addMessage("🚨 combined_server에서 응급 키워드 감지!")
                                if (response.smsSent) {
                                    addMessage("📱 응급 SMS 전송 완료")
                                }
                            }

                            addMessage(response.message)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(isRecording = false)
                            addMessage("❌ combined_server 음성 인식 실패: ${error.message}")
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isRecording = false)
                    addMessage("❌ 음성 녹음 실패")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRecording = false)
                addMessage("❌ 음성 인식 오류: ${e.message}")
                Log.e("MainViewModel", "음성 인식 오류", e)
            }
        }
    }

    fun sendManualEmergency() {
        if (!_uiState.value.isSTTActive) {
            addMessage("❌ 먼저 STT를 활성화해주세요")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🚨 combined_server로 수동 응급 호출 전송 중...")

        viewModelScope.launch {
            try {
                val result = sttRepository.sendEmergencySMS()

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        addMessage("🚨 수동 응급 호출 성공: ${response.message}")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        addMessage("❌ 응급 호출 실패: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ 응급 호출 오류: ${e.message}")
                Log.e("MainViewModel", "응급 호출 오류", e)
            }
        }
    }

    fun addMessage(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val formattedMessage = "[$timestamp] $message"

        _messages.value = (_messages.value + formattedMessage).takeLast(50)
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecorder?.release()
    }
}