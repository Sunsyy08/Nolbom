package com.project.nolbom.data.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import java.net.URISyntaxException

// LatLng 데이터 클래스 (충돌 방지)
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

// 서버와 주고받을 데이터 모델
data class LocationMessage(
    @SerializedName("type") val type: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: Long
)

data class UserLocationInfo(
    val userId: String,
    val userName: String,
    val location: LatLng,
    val timestamp: Long,
    val isOnline: Boolean = true
)

data class UsersListResponse(
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: List<UserData>
)

data class UserData(
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: Long
)

class WebSocketManager(
    private val serverUrl: String = "http://127.0.0.1:3000", // 맨 끝 슬래시 제거
    private val userId: String,
    private val userName: String
) {
    private var socket: Socket? = null
    private val gson = Gson()
    private val tag = "WebSocketManager"

    // 현재 온라인 사용자 목록
    private val connectedUsers = mutableMapOf<String, UserLocationInfo>()

    fun getLocationUpdates(): Flow<List<UserLocationInfo>> = callbackFlow {
        try {
            Log.d(tag, "서버 연결 시도: $serverUrl")

            // Socket.IO 클라이언트 초기화
            val opts = IO.Options().apply {
                timeout = 10000
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
            }

            socket = IO.socket(serverUrl, opts)

            // 연결 성공
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(tag, "✅ 서버 연결 성공")

                // 서버에 사용자 등록
                val joinData = JSONObject().apply {
                    put("userId", userId)
                    put("userName", userName)
                }
                socket?.emit("join", joinData)
                Log.d(tag, "👤 사용자 등록 요청: $userName ($userId)")
            }

            // 연결 실패
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(tag, "❌ 연결 실패: ${args.contentToString()}")
            }

            // 연결 해제
            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(tag, "🔴 연결 해제: ${args.contentToString()}")
            }

            // 현재 온라인 사용자 목록 수신
            socket?.on("users_list") { args ->
                try {
                    val jsonString = args[0].toString()
                    Log.d(tag, "📋 사용자 목록 수신: $jsonString")

                    val response = gson.fromJson(jsonString, UsersListResponse::class.java)
                    val userLocations = response.data.map { userData ->
                        UserLocationInfo(
                            userId = userData.userId,
                            userName = userData.userName,
                            location = LatLng(userData.latitude, userData.longitude),
                            timestamp = userData.timestamp
                        )
                    }

                    // 현재 사용자 목록 업데이트
                    connectedUsers.clear()
                    userLocations.forEach { user ->
                        connectedUsers[user.userId] = user
                    }

                    trySend(userLocations)

                } catch (e: Exception) {
                    Log.e(tag, "사용자 목록 파싱 오류: ${e.message}")
                }
            }

            // 개별 사용자 위치 업데이트 수신
            socket?.on("location_update") { args ->
                try {
                    val jsonString = args[0].toString()
                    Log.d(tag, "📍 위치 업데이트 수신: $jsonString")

                    val locationMsg = gson.fromJson(jsonString, LocationMessage::class.java)

                    // 자신의 위치는 제외
                    if (locationMsg.userId != userId) {
                        val userLocation = UserLocationInfo(
                            userId = locationMsg.userId,
                            userName = locationMsg.userName,
                            location = LatLng(locationMsg.latitude, locationMsg.longitude),
                            timestamp = locationMsg.timestamp
                        )

                        // 사용자 목록 업데이트
                        connectedUsers[locationMsg.userId] = userLocation

                        // 업데이트된 전체 목록 전송
                        trySend(connectedUsers.values.toList())
                    }

                } catch (e: Exception) {
                    Log.e(tag, "위치 업데이트 파싱 오류: ${e.message}")
                }
            }

            // 새 사용자 접속
            socket?.on("user_joined") { args ->
                try {
                    val jsonString = args[0].toString()
                    Log.d(tag, "👋 새 사용자 접속: $jsonString")

                    // 사용자 목록 재요청
                    requestUsersList()

                } catch (e: Exception) {
                    Log.e(tag, "사용자 접속 처리 오류: ${e.message}")
                }
            }

            // 사용자 해제
            socket?.on("user_left") { args ->
                try {
                    val jsonString = args[0].toString()
                    Log.d(tag, "👋 사용자 해제: $jsonString")

                    val leaveMsg = gson.fromJson(jsonString, LocationMessage::class.java)
                    connectedUsers.remove(leaveMsg.userId)

                    trySend(connectedUsers.values.toList())

                } catch (e: Exception) {
                    Log.e(tag, "사용자 해제 처리 오류: ${e.message}")
                }
            }

            // 서버 에러
            socket?.on("error") { args ->
                Log.e(tag, "🚨 서버 에러: ${args.contentToString()}")
            }

            // 연결 시작
            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(tag, "잘못된 서버 URL: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "WebSocket 초기화 오류: ${e.message}")
        }

        awaitClose {
            disconnect()
        }
    }

    fun sendLocation(location: LatLng) {
        try {
            val locationData = JSONObject().apply {
                put("userId", userId)
                put("userName", userName)
                put("latitude", location.latitude)
                put("longitude", location.longitude)
            }

            socket?.emit("location", locationData)
            Log.d(tag, "📤 위치 전송: ${location.latitude}, ${location.longitude}")

        } catch (e: Exception) {
            Log.e(tag, "위치 전송 오류: ${e.message}")
        }
    }

    fun requestUsersList() {
        try {
            socket?.emit("getUsers")
            Log.d(tag, "📋 사용자 목록 요청")
        } catch (e: Exception) {
            Log.e(tag, "사용자 목록 요청 오류: ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    fun disconnect() {
        try {
            Log.d(tag, "🔌 연결 해제 중...")
            socket?.disconnect()
            socket?.off()
            socket = null
            connectedUsers.clear()
        } catch (e: Exception) {
            Log.e(tag, "연결 해제 오류: ${e.message}")
        }
    }

    fun reconnect() {
        disconnect()
        // 잠시 후 재연결 시도는 Flow를 다시 구독하면 자동으로 실행됨
    }
}