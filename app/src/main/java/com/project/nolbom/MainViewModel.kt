// MainViewModel.kt - 자동 STT 활성화 개선 버전
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
    private var currentContext: Context? = null

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    init {
        loadUserProfile()
        loadSTTUserInfo() // 🔥 STT 사용자 정보도 로드
    }

    // ========== 기존 프로필 관련 코드 (그대로 유지) ==========
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            android.util.Log.d("MainViewModel", "프로필 로딩 시작")

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

    fun clearUserData() {
        userRepository.clearUserData()
        // 🔥 TokenStore도 함께 초기화
        TokenStore.clearUserData()
        _uiState.value = MainUiState()
        loadUserProfile()
    }

    fun saveTestData() {
        userRepository.saveTestData()
        loadUserProfile()
    }

    fun logUserData() {
        userRepository.logStoredUserData()
    }

    // ========== 🔥 STT 관련 코드 ==========

    fun initVoiceRecorder(context: Context) {
        currentContext = context
        voiceRecorder = VoiceRecorder(context)
    }

    fun setContext(context: Context) {
        currentContext = context
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

                // 🔥 회원가입 즉시 자동으로 STT 활성화
                delay(500) // 짧은 딜레이 후
                autoActivateSTTAfterSignup()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ 회원가입 실패: ${e.message}")
                Log.e("MainViewModel", "회원가입 실패", e)
            }
        }
    }

    // 🔥 회원가입 후 자동 STT 활성화
    private suspend fun autoActivateSTTAfterSignup() {
        addMessage("🔄 자동으로 STT 활성화 중...")

        try {
            // 1. 서버 연결 확인
            val healthResult = sttRepository.getServerStatus()
            healthResult.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(serverConnected = true)
                    addMessage("✅ 서버 연결 성공")

                    // 2. STT 활성화
                    val activateResult = sttRepository.activateSTT(enable = true)
                    activateResult.fold(
                        onSuccess = { activateResponse ->
                            if (activateResponse.success) {
                                _uiState.value = _uiState.value.copy(isSTTActive = true)
                                addMessage("✅ STT 자동 활성화 성공!")

                                // 🔥 STT 상태 저장
                                TokenStore.setSTTActive(true)

                                // 3. 실시간 서비스 시작
                                currentContext?.let { context ->
                                    startRealtimeVoiceService(context)
                                    addMessage("🎤 실시간 음성 감지 자동 시작됨")
                                }
                            } else {
                                addMessage("❌ STT 활성화 실패: ${activateResponse.message}")
                            }
                        },
                        onFailure = { error ->
                            addMessage("❌ STT 활성화 요청 실패: ${error.message}")
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(serverConnected = false)
                    addMessage("❌ 서버 연결 실패: ${error.message}")
                }
            )
        } catch (e: Exception) {
            addMessage("❌ 자동 STT 활성화 오류: ${e.message}")
            Log.e("MainViewModel", "자동 STT 활성화 오류", e)
        }
    }

    // 🔥 앱 시작 시 기존 사용자 자동 활성화 (홈화면 진입시)
    fun activateSTTIfNeeded() {
        // 이미 등록된 사용자이고, STT 복원이 필요한 경우
        if (_uiState.value.userRegistered && TokenStore.shouldRestoreSTT()) {
            addMessage("🔄 이전 세션 복원 - 자동으로 STT 재활성화 중...")

            viewModelScope.launch {
                autoActivateSTTAfterSignup() // 같은 로직 재사용
            }
        } else if (_uiState.value.userRegistered && TokenStore.shouldAutoStartSTT()) {
            // 회원가입 후 첫 실행인 경우
            addMessage("🔄 신규 사용자 - 자동으로 STT 활성화 중...")

            viewModelScope.launch {
                autoActivateSTTAfterSignup()
                // 자동 시작 플래그 해제 (한 번만 실행)
                TokenStore.setSTTAutoStart(false)
            }
        }
    }

    // 🔥 수동 STT 활성화 (버튼 클릭시)
    fun activateSTTAndStartService() {
        if (!_uiState.value.userRegistered) {
            addMessage("❌ 먼저 회원가입이 필요합니다")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🔄 수동 STT 활성화 및 실시간 감지 시작 중...")

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

                            addMessage("✅ STT 수동 활성화 성공!")

                            // 🔥 STT 상태 저장
                            TokenStore.setSTTActive(true)

                            // 실시간 음성 감지 서비스 시작
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

    // 🔥 STT 비활성화 (중지 버튼 클릭시)
    fun deactivateSTT() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        addMessage("🛑 STT 비활성화 중...")

        viewModelScope.launch {
            try {
                // 1. 먼저 서비스 중지
                currentContext?.let { context ->
                    stopRealtimeVoiceService(context)
                }

                // 2. 서버에 비활성화 요청
                val result = sttRepository.activateSTT(enable = false)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSTTActive = false
                )

                // 🔥 STT 상태 저장
                TokenStore.setSTTActive(false)

                addMessage("✅ STT 완전히 비활성화됨")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                addMessage("❌ STT 비활성화 오류: ${e.message}")
            }
        }
    }

    // 🔥 실시간 음성 감지 서비스 제어
    private fun startRealtimeVoiceService(context: Context) {
        try {
            val intent = Intent(context, RealtimeVoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            addMessage("🎤 실시간 음성 감지 시작됨 - 화면이 꺼져도 계속 작동합니다")
        } catch (e: Exception) {
            addMessage("❌ 실시간 서비스 시작 실패: ${e.message}")
            Log.e("MainViewModel", "실시간 서비스 시작 실패", e)
        }
    }

    private fun stopRealtimeVoiceService(context: Context) {
        try {
            val intent = Intent(context, RealtimeVoiceService::class.java)
            context.stopService(intent)
            addMessage("🛑 실시간 음성 감지 중지됨")
        } catch (e: Exception) {
            addMessage("❌ 실시간 서비스 중지 실패: ${e.message}")
            Log.e("MainViewModel", "실시간 서비스 중지 실패", e)
        }
    }

    fun checkServerHealth() {
        viewModelScope.launch {
            try {
                val result = sttRepository.getServerStatus()

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(serverConnected = true)
                        addMessage("✅ 서버 연결 성공 (활성 사용자: ${response.activeUsersCount}명)")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(serverConnected = false)
                        addMessage("❌ 서버 연결 실패: ${error.message}")
                        Log.e("MainViewModel", "서버 연결 실패", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(serverConnected = false)
                addMessage("❌ 서버 확인 오류: ${e.message}")
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
                    addMessage("📡 서버로 음성 데이터 전송 중...")

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
                                addMessage("🚨 서버에서 응급 키워드 감지!")
                                if (response.smsSent) {
                                    addMessage("📱 응급 SMS 전송 완료")
                                }
                            }

                            addMessage(response.message)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(isRecording = false)
                            addMessage("❌ 음성 인식 실패: ${error.message}")
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
        addMessage("🚨 수동 응급 호출 전송 중...")

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