package com.project.nolbom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.nolbom.data.location.LocationManager
import com.project.nolbom.data.network.UserLocationInfo
import com.project.nolbom.data.network.WebSocketManager
import com.project.nolbom.data.network.LatLng
import com.project.nolbom.data.local.TokenStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LocationState(
    val currentLocation: LatLng? = null,
    val locationHistory: List<LatLng> = emptyList(),
    val isLocationEnabled: Boolean = false,
    val isTracking: Boolean = false,
    val error: String? = null,
    // 멀티유저 기능
    val otherUsers: List<UserLocationInfo> = emptyList(),
    val isConnectedToServer: Boolean = false,
    val totalUsersCount: Int = 0,
    val userId: String = "",
    val userName: String = ""
)

class LocationViewModel(
    private val locationManager: LocationManager,
    private val serverUrl: String = "http://127.0.0.1:3000" // 실제 서버 IP로 변경 필요
) : ViewModel() {

    // 🔥 TokenStore에서 사용자 정보 가져오기
    private val userId: String = TokenStore.getOrCreateLocationUserId()
    private val userName: String = TokenStore.getUserName() ?: "놀봄이${(1000..9999).random()}"

    private val webSocketManager = WebSocketManager(
        serverUrl = serverUrl,
        userId = userId,
        userName = userName
    )

    private val _locationState = MutableStateFlow(
        LocationState(
            userId = userId,
            userName = userName
        )
    )
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    init {
        // 앱 시작 시 WebSocket 연결
        connectToServer()
    }

    private fun connectToServer() {
        viewModelScope.launch {
            try {
                webSocketManager.getLocationUpdates()
                    .catch { exception ->
                        _locationState.update {
                            it.copy(
                                error = "서버 연결 오류: ${exception.message}",
                                isConnectedToServer = false
                            )
                        }
                    }
                    .collect { otherUsers ->
                        _locationState.update { currentState ->
                            currentState.copy(
                                otherUsers = otherUsers,
                                isConnectedToServer = webSocketManager.isConnected(),
                                totalUsersCount = otherUsers.size + 1, // 자신 포함
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(
                        error = "WebSocket 초기화 실패: ${e.message}",
                        isConnectedToServer = false
                    )
                }
            }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                _locationState.update { it.copy(isTracking = true, error = null) }

                locationManager.getLocationUpdates()
                    .catch { exception ->
                        _locationState.update {
                            it.copy(
                                error = "위치 업데이트 오류: ${exception.message}",
                                isTracking = false
                            )
                        }
                    }
                    .collect { location ->
                        val latLng = LatLng(location.latitude, location.longitude)

                        // 서버에 자신의 위치 전송
                        if (webSocketManager.isConnected()) {
                            webSocketManager.sendLocation(latLng)
                        }

                        _locationState.update { currentState ->
                            // 위치 기록 저장 (최대 100개)
                            val updatedHistory = currentState.locationHistory.toMutableList().apply {
                                add(latLng)
                                if (size > 100) removeAt(0)
                            }

                            currentState.copy(
                                currentLocation = latLng,
                                locationHistory = updatedHistory,
                                isLocationEnabled = true,
                                isConnectedToServer = webSocketManager.isConnected(),
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(
                        error = "위치 서비스 시작 실패: ${e.message}",
                        isTracking = false
                    )
                }
            }
        }
    }

    fun stopLocationUpdates() {
        _locationState.update {
            it.copy(
                isTracking = false,
                isLocationEnabled = false
            )
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = locationManager.getCurrentLocation()
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)

                    // 서버에 현재 위치 전송
                    if (webSocketManager.isConnected()) {
                        webSocketManager.sendLocation(latLng)
                    }

                    _locationState.update { currentState ->
                        currentState.copy(
                            currentLocation = latLng,
                            isLocationEnabled = true,
                            error = null
                        )
                    }
                } ?: run {
                    _locationState.update {
                        it.copy(error = "현재 위치를 가져올 수 없습니다")
                    }
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(error = "위치 가져오기 실패: ${e.message}")
                }
            }
        }
    }

    fun getLastKnownLocation() {
        viewModelScope.launch {
            try {
                val location = locationManager.getLastKnownLocation()
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _locationState.update { currentState ->
                        currentState.copy(
                            currentLocation = latLng,
                            isLocationEnabled = true
                        )
                    }
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(error = "마지막 위치 가져오기 실패: ${e.message}")
                }
            }
        }
    }

    fun reconnectToServer() {
        viewModelScope.launch {
            _locationState.update { it.copy(error = "서버 재연결 중...") }
            webSocketManager.reconnect()
            connectToServer()
        }
    }

    fun refreshUsersList() {
        if (webSocketManager.isConnected()) {
            webSocketManager.requestUsersList()
        }
    }

    fun clearError() {
        _locationState.update { it.copy(error = null) }
    }

    fun clearLocationHistory() {
        _locationState.update { it.copy(locationHistory = emptyList()) }
    }

    // 특정 사용자 위치로 지도 이동
    fun focusOnUser(userId: String): LatLng? {
        return _locationState.value.otherUsers.find { it.userId == userId }?.location
    }

    // 모든 사용자가 보이도록 지도 범위 계산
    fun getAllUsersLocationBounds(): Pair<LatLng?, LatLng?> {
        val allLocations = mutableListOf<LatLng>()

        // 자신의 위치 추가
        _locationState.value.currentLocation?.let { allLocations.add(it) }

        // 다른 사용자들 위치 추가
        allLocations.addAll(_locationState.value.otherUsers.map { it.location })

        if (allLocations.isEmpty()) return null to null

        val minLat = allLocations.minOf { it.latitude }
        val maxLat = allLocations.maxOf { it.latitude }
        val minLng = allLocations.minOf { it.longitude }
        val maxLng = allLocations.maxOf { it.longitude }

        return LatLng(minLat, minLng) to LatLng(maxLat, maxLng)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}