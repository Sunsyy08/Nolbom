package com.project.nolbom.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.project.nolbom.data.network.UserLocationInfo
import com.project.nolbom.data.network.LatLng

@Composable
fun KakaoMapView(
    currentLocation: LatLng?,
    locationHistory: List<LatLng>,
    otherUsers: List<UserLocationInfo>,
    modifier: Modifier = Modifier,
    onMapReady: (Any?) -> Unit = {},
    onUserMarkerClick: (UserLocationInfo) -> Unit = {}
) {
    val context = LocalContext.current

    // 카카오맵 SDK 문제로 인해 임시 플레이스홀더 사용
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E8)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // 지도 영역 표시
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🗺️ 카카오맵 영역",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "지도 표시 준비 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 현재 위치 정보 카드
            currentLocation?.let { location ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📍 내 현재 위치",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "위도: ${String.format("%.6f", location.latitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "경도: ${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (locationHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📊 이동 기록: ${locationHistory.size}개 지점",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 다른 사용자들 정보 카드
            if (otherUsers.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "👥 온라인 사용자 (${otherUsers.size}명)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        otherUsers.take(5).forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${user.userName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format("%.4f", user.location.latitude)}, ${String.format("%.4f", user.location.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (otherUsers.size > 5) {
                            Text(
                                text = "외 ${otherUsers.size - 5}명",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "👤 현재 온라인 사용자가 없습니다",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF8F00)
                    )
                }
            }

            // 상태 정보
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "ℹ️ 개발 노트",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 위치 추적 기능: 정상 작동\n• 실시간 서버 연동: 정상 작동\n• 카카오맵 표시: 개발 중",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7B1FA2)
                    )
                }
            }
        }
    }
}

// 카카오맵 SDK가 제대로 설정되면 사용할 실제 지도 컴포넌트
@Composable
private fun RealKakaoMapView(
    currentLocation: LatLng?,
    locationHistory: List<LatLng>,
    otherUsers: List<UserLocationInfo>,
    modifier: Modifier = Modifier,
    onMapReady: (Any?) -> Unit = {},
    onUserMarkerClick: (UserLocationInfo) -> Unit = {}
) {
    // 실제 카카오맵 구현은 SDK 설정 완료 후 추가
    // 현재는 vectormap import 에러로 인해 주석 처리
    /*
    val context = LocalContext.current
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    AndroidView(
        factory = { ctx ->
            // 실제 카카오맵 MapView 생성
            // KakaoMapView(ctx).apply { ... }
        },
        modifier = modifier.fillMaxSize()
    )
    */
}

// 유틸리티 함수들
fun LatLng.toDisplayString(): String {
    return "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
}