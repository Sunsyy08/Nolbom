// MainViewModel.kt
package com.project.nolbom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.repository.UserRepository

// UI 상태 클래스
data class MainUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val profileBitmap: Bitmap? = null,
    val error: String? = null
)

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

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
}