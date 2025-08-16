package com.project.nolbom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.nolbom.data.repository.MissingPersonsRepository
import com.project.nolbom.list.AlertUser
import com.project.nolbom.list.toAlertUserList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertListViewModel : ViewModel() {

    private val repository = MissingPersonsRepository()

    private val _uiState = MutableStateFlow(AlertListUiState())
    val uiState: StateFlow<AlertListUiState> = _uiState.asStateFlow()

    private val _alertUsers = MutableStateFlow<List<AlertUser>>(emptyList())
    val alertUsers: StateFlow<List<AlertUser>> = _alertUsers.asStateFlow()

    init {
        loadMissingPersons()
    }

    // 백엔드에서 실종자 목록 로드
    fun loadMissingPersons() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getMissingPersons(status = "MISSING").fold(
                onSuccess = { response ->
                    // 🆕 백엔드 데이터를 기존 AlertUser 형식으로 변환
                    val alertUserList = response.data.toAlertUserList()
                    _alertUsers.value = alertUserList

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalCount = response.total,
                        isConnectedToBackend = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnectedToBackend = false,
                        error = "백엔드 연결 실패: ${exception.message}"
                    )
                }
            )
        }
    }

    // 새로고침
    fun refresh() {
        loadMissingPersons()
    }

    // 에러 메시지 클리어
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AlertListUiState(
    val isLoading: Boolean = false,
    val totalCount: Int = 0,
    val isConnectedToBackend: Boolean = false,
    val error: String? = null
)