package com.project.nolbom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.*
import com.project.nolbom.data.location.LocationManager
import com.project.nolbom.data.network.UserLocationInfo
import com.project.nolbom.map.KakaoMapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FullScreenMapScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    // 위치 추적 ViewModel
    val locationManager = remember { LocationManager(context) }
    val locationViewModel = remember {
        LocationViewModel(
            locationManager = locationManager,
            serverUrl = "http://127.0.0.1:3000" // 실제 서버 IP로 변경
        )
    }
    val locationState by locationViewModel.locationState.collectAsState()

    // 위치 권한 상태
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한이 허용되면 마지막 위치 가져오기
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            locationViewModel.getLastKnownLocation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissions.allPermissionsGranted) {
            // 전체화면 카카오맵
            KakaoMapView(
                currentLocation = locationState.currentLocation,
                locationHistory = locationState.locationHistory,
                otherUsers = locationState.otherUsers,
                modifier = Modifier.fillMaxSize(),
                onUserMarkerClick = { user ->
                    // 사용자 마커 클릭 시 처리 (옵션)
                }
            )
        } else {
            // 권한 요청 화면
            PermissionRequestFullScreen(
                onRequestPermission = {
                    locationPermissions.launchMultiplePermissionRequest()
                }
            )
        }

        // 상단 오버레이: 뒤로가기 버튼 + 상태 정보
        TopOverlay(
            isConnected = locationState.isConnectedToServer,
            totalUsers = locationState.totalUsersCount,
            currentLocation = locationState.currentLocation,
            userName = locationState.userName,
            onBackClick = { navController.popBackStack() }
        )

        // 하단 오버레이: 컨트롤 버튼들
        BottomOverlay(
            isTracking = locationState.isTracking,
            isLocationEnabled = locationState.isLocationEnabled,
            isConnected = locationState.isConnectedToServer,
            onStartTracking = {
                if (locationPermissions.allPermissionsGranted) {
                    locationViewModel.startLocationUpdates()
                } else {
                    locationPermissions.launchMultiplePermissionRequest()
                }
            },
            onStopTracking = { locationViewModel.stopLocationUpdates() },
            onCurrentLocation = { locationViewModel.getCurrentLocation() },
            onRefreshUsers = { locationViewModel.refreshUsersList() }
        )

        // 오른쪽 사이드 오버레이: 온라인 사용자 목록
        if (locationState.otherUsers.isNotEmpty()) {
            RightSideOverlay(
                users = locationState.otherUsers,
                onUserClick = { user ->
                    // 해당 사용자 위치로 지도 이동 (추후 구현 가능)
                }
            )
        }

        // 에러 메시지 (중앙 상단)
        locationState.error?.let { error ->
            ErrorOverlay(
                error = error,
                onDismiss = { locationViewModel.clearError() }
            )
        }
    }
}

@Composable
fun TopOverlay(
    isConnected: Boolean,
    totalUsers: Int,
    currentLocation: com.project.nolbom.data.network.LatLng?,
    userName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // 뒤로가기 버튼
        FloatingActionButton(
            onClick = onBackClick,
            modifier = Modifier.size(56.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }

        // 상태 정보 카드
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = "연결 상태",
                        tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isConnected) "$userName • $totalUsers 명" else "연결 안됨",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                currentLocation?.let { location ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomOverlay(
    isTracking: Boolean,
    isLocationEnabled: Boolean,
    isConnected: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onCurrentLocation: () -> Unit,
    onRefreshUsers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 추적 시작/중지 버튼
                FloatingActionButton(
                    onClick = if (isTracking) onStopTracking else onStartTracking,
                    containerColor = if (isTracking) Color(0xFFFF5722) else Color(0xFF4CAF50),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isTracking) "추적 중지" else "추적 시작",
                        tint = Color.White
                    )
                }

                // 현재 위치 버튼
                FloatingActionButton(
                    onClick = onCurrentLocation,
                    containerColor = Color(0xFF2196F3),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "현재 위치",
                        tint = Color.White
                    )
                }

                // 사용자 새로고침 버튼
                FloatingActionButton(
                    onClick = {
                        if (isConnected) {
                            onRefreshUsers()
                        }
                    },
                    containerColor = if (isConnected) Color(0xFF9C27B0) else Color.Gray,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "사용자 새로고침",
                        tint = Color.White
                    )
                }

                // 전체 보기 버튼
                FloatingActionButton(
                    onClick = { /* 전체 보기 기능 - 추후 구현 */ },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusWeak,
                        contentDescription = "전체 보기",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RightSideOverlay(
    users: List<UserLocationInfo>,
    onUserClick: (UserLocationInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 16.dp, top = 100.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .padding(8.dp)
            ) {
                Text(
                    text = "🟢 온라인 (${users.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                users.take(5).forEach { user -> // 최대 5명만 표시
                    UserMiniCard(
                        user = user,
                        onClick = { onUserClick(user) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (users.size > 5) {
                    Text(
                        text = "외 ${users.size - 5}명",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserMiniCard(
    user: UserLocationInfo,
    onClick: () -> Unit
) {
    // 사용자별 색상
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFD32F2F),
        Color(0xFF7B1FA2), Color(0xFFF57C00), Color(0xFF0288D1)
    )
    val userColor = colors[user.userId.hashCode().mod(colors.size)]

    Card(
        colors = CardDefaults.cardColors(
            containerColor = userColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(userColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = user.userName,
                style = MaterialTheme.typography.labelSmall,
                color = userColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ErrorOverlay(
    error: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF5722).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "에러",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestFullScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF83E3BD)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "위치 권한이 필요합니다",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "실시간 위치 추적 및 지도 표시를 위해\n위치 권한을 허용해주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF83E3BD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("권한 허용하기")
                }
            }
        }
    }
}