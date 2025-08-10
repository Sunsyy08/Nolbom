package com.project.nolbom.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.project.nolbom.data.network.UserLocationInfo
import com.project.nolbom.data.network.LatLng
import android.util.Log

@Composable
fun KakaoMapView(
    currentLocation: LatLng?,
    locationHistory: List<LatLng>,
    otherUsers: List<UserLocationInfo>,
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap?) -> Unit = {},
    onUserMarkerClick: (UserLocationInfo) -> Unit = {}
) {
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }

    AndroidView(
        factory = { context ->
            Log.d("KakaoMapView", "지도 초기화 시작")
            try {
                val mapViewInstance = MapView(context)
                mapView = mapViewInstance

                mapViewInstance.start(object : com.kakao.vectormap.MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        Log.d("KakaoMapView", "지도 destroy")
                    }

                    override fun onMapError(error: Exception) {
                        Log.e("KakaoMapView", "지도 오류: ${error.message}")
                        mapError = "지도 로딩 실패: ${error.message}"
                    }
                }, object : KakaoMapReadyCallback() {
                    override fun onMapReady(map: KakaoMap) {
                        Log.d("KakaoMapView", "지도 준비 완료")
                        kakaoMap = map
                        isMapReady = true
                        onMapReady(map)

                        // 초기 카메라 설정
                        val initialPosition = currentLocation?.let {
                            KakaoLatLng.from(it.latitude, it.longitude)
                        } ?: KakaoLatLng.from(37.5665, 126.9780) // 서울시청

                        map.moveCamera(
                            CameraUpdateFactory.newCenterPosition(initialPosition, 15)
                        )
                    }

                    override fun getPosition(): KakaoLatLng {
                        return KakaoLatLng.from(37.5665, 126.9780)
                    }
                })

                mapViewInstance as View // MapView를 View로 캐스팅

            } catch (e: Exception) {
                Log.e("KakaoMapView", "MapView 생성 실패: ${e.message}")
                mapError = "지도 초기화 실패: ${e.message}"

                // 에러 발생 시 빈 View 반환
                View(context)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view: View ->
            if (isMapReady && kakaoMap != null) {
                kakaoMap?.let { map ->
                    try {
                        // 기존 마커들 모두 제거
                        clearAllMarkers(map)

                        // 현재 위치 업데이트
                        currentLocation?.let { location ->
                            showCurrentLocationMarker(map, location)
                        }

                        // 다른 사용자들 마커 표시
                        showOtherUsersMarkers(map, otherUsers)

                        // 이동 경로 표시
                        showLocationHistory(map, locationHistory)

                    } catch (e: Exception) {
                        Log.e("KakaoMapView", "지도 업데이트 오류: ${e.message}")
                    }
                }
            }
        }
    )

    // 지도 로딩 중이거나 에러가 발생한 경우
    if (!isMapReady || mapError != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E8)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (mapError != null) Color(0xFFFFEBEE) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mapError != null) {
                        // 에러 표시
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "에러",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "지도 로딩 실패",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mapError ?: "알 수 없는 오류",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "카카오 API 키를 확인해주세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    } else {
                        // 로딩 표시
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "카카오맵 로딩 중...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "지도를 준비하고 있습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // 메모리 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                Log.d("KakaoMapView", "지도 정리 중")
                mapView?.finish()
            } catch (e: Exception) {
                Log.e("KakaoMapView", "지도 정리 오류: ${e.message}")
            }
        }
    }
}

// 모든 마커 제거
private fun clearAllMarkers(map: KakaoMap) {
    try {
        val labelManager = map.labelManager
        labelManager?.layer?.removeAll()
    } catch (e: Exception) {
        Log.e("KakaoMapView", "마커 제거 오류: ${e.message}")
    }
}

// 현재 위치 마커 표시
private fun showCurrentLocationMarker(map: KakaoMap, location: LatLng) {
    try {
        val labelManager = map.labelManager ?: return

        // 현재 위치 마커 추가
        val labelStyle = LabelStyle.from(android.R.drawable.ic_menu_mylocation)
        val labelStyles = LabelStyles.from(labelStyle)

        val labelOptions = LabelOptions.from(KakaoLatLng.from(location.latitude, location.longitude))
            .setStyles(labelStyles)
            .setTexts("내 위치")

        labelManager.layer?.addLabel(labelOptions)

        // 카메라를 현재 위치로 이동
        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(
                KakaoLatLng.from(location.latitude, location.longitude),
                16
            )
        )

        Log.d("KakaoMapView", "현재 위치 마커 표시: ${location.latitude}, ${location.longitude}")

    } catch (e: Exception) {
        Log.e("KakaoMapView", "현재 위치 마커 표시 오류: ${e.message}")
    }
}

// 다른 사용자들 마커 표시
private fun showOtherUsersMarkers(map: KakaoMap, otherUsers: List<UserLocationInfo>) {
    try {
        val labelManager = map.labelManager ?: return

        // 사용자별 다른 아이콘 사용
        val userIcons = listOf(
            android.R.drawable.ic_dialog_map,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_directions,
            android.R.drawable.ic_menu_mapmode,
            android.R.drawable.ic_menu_gallery
        )

        otherUsers.forEachIndexed { index, user ->
            val userIcon = userIcons[index % userIcons.size]

            val labelStyle = LabelStyle.from(userIcon)
            val labelStyles = LabelStyles.from(labelStyle)

            val labelOptions = LabelOptions.from(KakaoLatLng.from(user.location.latitude, user.location.longitude))
                .setStyles(labelStyles)
                .setTexts(user.userName)

            labelManager.layer?.addLabel(labelOptions)
        }

        Log.d("KakaoMapView", "다른 사용자 마커 표시: ${otherUsers.size}명")

    } catch (e: Exception) {
        Log.e("KakaoMapView", "사용자 마커 표시 오류: ${e.message}")
    }
}

// 이동 경로 표시
private fun showLocationHistory(map: KakaoMap, locationHistory: List<LatLng>) {
    try {
        if (locationHistory.isEmpty()) return

        val labelManager = map.labelManager ?: return

        // 최근 10개 지점만 표시 (성능을 위해)
        locationHistory.takeLast(10).forEachIndexed { index, location ->
            if (index > 0) { // 첫 번째는 현재 위치와 겹칠 수 있으므로 건너뜀
                val labelStyle = LabelStyle.from(android.R.drawable.ic_menu_recent_history)
                val labelStyles = LabelStyles.from(labelStyle)

                val labelOptions = LabelOptions.from(KakaoLatLng.from(location.latitude, location.longitude))
                    .setStyles(labelStyles)
                    .setTexts("${index}")

                labelManager.layer?.addLabel(labelOptions)
            }
        }

        Log.d("KakaoMapView", "이동 경로 표시: ${locationHistory.size}개 지점")

    } catch (e: Exception) {
        Log.e("KakaoMapView", "이동 경로 표시 오류: ${e.message}")
    }
}

// 유틸리티 함수
fun LatLng.toDisplayString(): String {
    return "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
}