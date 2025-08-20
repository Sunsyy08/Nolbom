package com.project.nolbom

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.nolbom.list.AlertUser
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.accompanist.flowlayout.FlowRow
import com.project.nolbom.data.repository.STTRepository
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.utils.VoiceRecorder
import com.project.nolbom.utils.RequestAudioPermission
import com.project.nolbom.utils.hasAudioPermission

// API 연동을 위한 추가 imports
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

// 위치 추적 관련 imports
import com.google.accompanist.permissions.*
import com.project.nolbom.data.location.LocationManager
import com.project.nolbom.data.network.UserLocationInfo
import com.project.nolbom.map.KakaoMapView
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.repository.UserRepository

import android.util.Base64
import coil.request.ImageRequest
import com.project.nolbom.data.network.RetrofitClient


// 전화 앱 실행을 위한 함수
fun openPhoneApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_DIAL)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun callPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadUsersFromAssets(context: Context): List<AlertUser> {
    val jsonString = context.assets.open("user.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val type = object : TypeToken<List<AlertUser>>() {}.type
    return gson.fromJson(jsonString, type)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onNavigateToAlertList: () -> Unit
) {


    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 🆕 백엔드 연동 ViewModel 추가
    val alertListViewModel: AlertListViewModel = viewModel()
    val alertUiState by alertListViewModel.uiState.collectAsState()
    val backendAlertUsers by alertListViewModel.alertUsers.collectAsState()

    // 기존 ViewModel
    val mainViewModel: MainViewModel = viewModel {
        MainViewModel(UserRepository(context))
    }
    val uiState by mainViewModel.uiState.collectAsState()

    // 위치 추적 ViewModel 추가
    val locationManager = remember { LocationManager(context) }
    val locationViewModel = remember {
        LocationViewModel(
            locationManager = locationManager,
            serverUrl = RetrofitClient.getWebSocketUrl()
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

    // 🆕 사용자 리스트 - 백엔드 우선, 실패시 JSON
    val jsonUserList = remember { loadUsersFromAssets(context) }
    val userList = if (alertUiState.isConnectedToBackend) backendAlertUsers else jsonUserList

    // JSON에서 사용자 리스트 읽기
//    val userList = remember {
//        loadUsersFromAssets(context)
//    }

    // 권한이 허용되면 마지막 위치 가져오기
    androidx.compose.runtime.LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            locationViewModel.getLastKnownLocation()
        }
    }

    // 🔥 STT 관련 변수 추가
    val messages by mainViewModel.messages.collectAsState()
    var showSignupDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("01044573420") }

    // 🔥 VoiceRecorder 초기화 및 STT 자동 활성화 로직
    LaunchedEffect(Unit) {
        // 1. Context 설정 및 VoiceRecorder 초기화
        mainViewModel.setContext(context)
        mainViewModel.initVoiceRecorder(context)

        // 2. 사용자 등록 상태 확인
        if (!mainViewModel.isUserRegistered()) {
            // 미등록 사용자 - 회원가입 다이얼로그 표시
            showSignupDialog = true
        } else {
            // 🔥 기존 등록 사용자 - 자동으로 STT 활성화
            mainViewModel.checkServerHealth()
            mainViewModel.activateSTTIfNeeded() // 자동 활성화 함수 호출
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // ← 이거 추가 (기존 padding 대신)
            .verticalScroll(scrollState)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로딩 상태 표시
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF83E3BD)
            )
        }
        // 🆕 백엔드 연결 상태 카드 추가
        if (!alertUiState.isConnectedToBackend) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEB3B).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFF9800)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "실종자 목록 오프라인 모드",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "저장된 데이터를 표시합니다",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { alertListViewModel.refresh() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "재연결",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 에러 상태 표시
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "프로필 로드 실패",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Button(
                            onClick = { mainViewModel.retryLoadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF83E3BD))
                        ) {
                            Text("다시 시도")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { mainViewModel.clearUserData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("데이터 초기화")
                        }
                    }
                }
            }
        }

        // 상단 프로필 헤더
        ProfileHeaderWithData(
            userProfile = uiState.userProfile,
            profileBitmap = uiState.profileBitmap
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 STT 컨트롤 카드 - 등록된 사용자에게만 표시
        if (uiState.userRegistered) {
            RequestAudioPermission(
                onPermissionGranted = { mainViewModel.addMessage("✅ 마이크 권한 승인됨") },
                onPermissionDenied = { mainViewModel.addMessage("❌ 마이크 권한이 필요합니다") }
            ) { requestPermission ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isSTTActive)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            Color(0xFFFFEB3B).copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.isSTTActive)
                                        "🎤 실시간 음성 감지 중"
                                    else
                                        "🔇 음성 감지 대기 중",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isSTTActive) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                                Text(
                                    text = if (uiState.isSTTActive)
                                        "화면이 꺼져도 계속 작동 중입니다"
                                    else
                                        "STT가 비활성화되어 있습니다",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                // 마지막 인식 결과 표시
                                if (uiState.lastTranscript.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "최근 인식: ${uiState.lastTranscript}",
                                        fontSize = 11.sp,
                                        color = if (uiState.keywordDetected) Color(0xFFD32F2F) else Color(0xFF666666),
                                        maxLines = 1
                                    )
                                }
                            }

                            // 실시간 상태 표시
                            Surface(
                                color = if (uiState.isSTTActive) Color(0xFF4CAF50) else Color(0xFFFFEB3B),
                                shape = CircleShape,
                                modifier = Modifier.size(12.dp)
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔥 STT 제어 버튼들 - 자동 활성화 고려한 UI
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.isSTTActive) {
                                // STT가 활성화된 상태 - 비활성화 버튼
                                Button(
                                    onClick = { mainViewModel.deactivateSTT() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("완전 중지", fontSize = 12.sp)
                                    }
                                }

                                // 수동 테스트 버튼 (기존 방식)
                                Button(
                                    onClick = {
                                        if (hasAudioPermission(context)) {
                                            mainViewModel.startVoiceRecognition()
                                        } else {
                                            requestPermission()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isRecording,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    if (uiState.isRecording) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("테스트", fontSize = 12.sp)
                                    }
                                }

                            } else {
                                // STT가 비활성화된 상태 - 수동 재활성화 버튼
                                Button(
                                    onClick = {
                                        if (hasAudioPermission(context)) {
                                            mainViewModel.activateSTTAndStartService()
                                        } else {
                                            requestPermission()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("실시간 음성 감지 재시작", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
        // 🔥 수동 응급 호출 버튼은 STT 활성화 상태에서만 표시
        if (uiState.isSTTActive) {
            Button(
                onClick = { mainViewModel.sendManualEmergency() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("수동 응급 호출", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 위치 연결 상태 표시 카드
        LocationConnectionCard(
            isConnected = locationState.isConnectedToServer,
            totalUsers = locationState.totalUsersCount,
            isTracking = locationState.isTracking,
            currentUserName = locationState.userName,
            onStartTracking = {
                if (locationPermissions.allPermissionsGranted) {
                    locationViewModel.startLocationUpdates()
                } else {
                    locationPermissions.launchMultiplePermissionRequest()
                }
            },
            onStopTracking = { locationViewModel.stopLocationUpdates() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 중간 영역: 왼쪽 리스트, 오른쪽 카카오맵
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(400.dp)
        ) {
            // 왼쪽: 사용자 리스트
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF83E3BD), RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userList) { user ->
                        AlertCardSmall(user)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 오른쪽: 카카오맵 (클릭하면 전체화면으로)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        // 전체화면 지도로 네비게이션
                        navController.navigate("fullmap")
                    }
            ) {
                if (locationPermissions.allPermissionsGranted) {
                    KakaoMapView(
                        currentLocation = locationState.currentLocation,
                        locationHistory = locationState.locationHistory,
                        otherUsers = locationState.otherUsers,
                        modifier = Modifier.fillMaxSize()
                    ) { kakaoMap ->
                        // 미니맵이므로 기본 설정만
                    }
                } else {
                    // 권한 없을 때 표시
                    LocationPermissionRequest(
                        onRequestPermission = {
                            locationPermissions.launchMultiplePermissionRequest()
                        }
                    )
                }

                // 클릭 안내 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "전체화면",
                                tint = Color(0xFF83E3BD),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📍 지도 보기",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "터치하면 전체화면",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 액션 카드 섹션 (기존 유지)
        ActionCardSection(onNavigateToAlertList)

        Spacer(modifier = Modifier.weight(1f))

        // 하단 탭바 (기존 유지)
        BottomTabBar(
            profileBitmap = uiState.profileBitmap,
            onPhoneClick = { openPhoneApp(context) },
            onTabSelected = { tab ->
                when (tab) {
                    TabItem.Profile -> navController.navigate("profile") // Screen.Profile.route 대신
                    else -> { /* 다른 탭 동작 */ }
                }
            }
        )
    }
    // 🔥 STT 회원가입 다이얼로그 - 회원가입 즉시 자동 활성화
    if (showSignupDialog) {
        AlertDialog(
            onDismissRequest = { /* 회원가입 필수 */ },
            title = {
                Text(
                    "음성 응급 감지 설정",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            },
            text = {
                Column {
                    Text(
                        "회원가입 완료 후 자동으로 실시간 음성 감지가 시작됩니다.",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("이름") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF83E3BD),
                            focusedLabelColor = Color(0xFF83E3BD)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userPhone,
                        onValueChange = { userPhone = it },
                        label = { Text("응급 연락처") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF83E3BD),
                            focusedLabelColor = Color(0xFF83E3BD)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "※ 화면이 꺼져도 백그라운드에서 계속 작동합니다",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userName.isNotBlank() && userPhone.isNotBlank()) {
                            mainViewModel.registerUser(userName, userPhone)
                            showSignupDialog = false
                        }
                    },
                    enabled = userName.isNotBlank() && userPhone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("가입 후 자동 시작", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    Spacer(modifier = Modifier.height(80.dp))
}

// 위치 연결 상태를 보여주는 카드
@Composable
fun LocationConnectionCard(
    isConnected: Boolean,
    totalUsers: Int,
    isTracking: Boolean,
    currentUserName: String,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = "연결 상태",
                tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "🟢 실시간 위치 공유 중" else "🟡 위치 공유 준비 중",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isConnected) "$currentUserName • 총 $totalUsers 명 접속" else "서버 연결을 확인해주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // 추적 시작/중지 버튼
            Button(
                onClick = if (isTracking) onStopTracking else onStartTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFFF5722) else Color(0xFF83E3BD)
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isTracking) "중지" else "시작",
                    fontSize = 12.sp
                )
            }
        }
    }
}

// 위치 권한 요청 컴포넌트
@Composable
fun LocationPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = "위치 권한 필요",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "위치 권한이\n필요합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF83E3BD)
                    )
                ) {
                    Text("권한 허용", fontSize = 12.sp)
                }
            }
        }
    }
}

// 기존 함수들 유지 (ProfileHeaderWithData, AlertCardSmall, ActionCardSection, etc.)
@Composable
fun ProfileHeaderWithData(
    userProfile: UserProfile?,
    profileBitmap: Bitmap?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "기본 프로필 이미지",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userProfile?.name ?: "사용자",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userProfile?.home_address ?: "주소 정보 없음",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp)
            )
        }
    }
}

// base64 문자열을 Bitmap으로 변환하는 함수
fun base64ToBitmap(base64String: String?): Bitmap? {
    if (base64String == null || base64String.isEmpty()) return null

    return try {
        // "data:image/jpeg;base64," 부분 제거
        val base64Data = if (base64String.contains(",")) {
            base64String.substring(base64String.indexOf(",") + 1)
        } else {
            base64String
        }

        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        println("❌ Base64 → Bitmap 변환 실패: ${e.message}")
        null
    }
}

// 2. AlertCardSmall 함수 수정 (상단 프로필 방식 적용)

@Composable
fun AlertCardSmall(user: AlertUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // 🔍 프로필 이미지 상태 확인
        println("🖼️ AlertCardSmall - ${user.name} 프로필 이미지: ${user.profile_image?.take(50) ?: "없음"}")

        // ✅ 상단 프로필과 동일한 방식으로 변경
        val profileBitmap = remember(user.profile_image) {
            base64ToBitmap(user.profile_image)
        }

        if (profileBitmap != null) {
            println("✅ ${user.name}: Bitmap 변환 성공")
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "${user.name} 프로필",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            println("📷 기본 프로필 이미지 사용 - ${user.name}")
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "기본 프로필",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        FlowRow(
            mainAxisSpacing = 4.dp,
            crossAxisSpacing = 4.dp
        ) {
            Text(
                text = user.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // 성별 정보 추가
            user.gender?.let { gender ->
                Text(
                    text = "성별: $gender,",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "${user.age},",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = "${user.height},",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = "${user.weight},",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = "위치: ${user.location}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}


@Composable
fun ActionCardSection(onNavigateToAlertList: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF83E3BD), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionCard(
                title = "얼굴 등록",
                icon = Icons.Default.Face,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            )
            ActionCard(
                title = "위치 보기",
                icon = Icons.Default.LocationOn,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).clickable {
                    // 전체화면 지도로 이동하는 기능 추가 가능
                }
            )
            ActionCard(
                title = "안내 문자",
                icon = Icons.Default.MailOutline,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).clickable { onNavigateToAlertList() }
            )
        }
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = Color(0xFF83E3BD)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
            )
        }
    }
}

@Composable
fun BottomTabBar(
    modifier: Modifier = Modifier,
    profileBitmap: Bitmap? = null, // ← 매개변수 추가
    onTabSelected: (TabItem) -> Unit = {},
    selectedTab: TabItem = TabItem.Home,
    onPhoneClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val token = TokenStore.getToken()

    val tabs = listOf(
        TabItem.Profile,
        TabItem.Call,
        TabItem.Home,
        TabItem.Chat,
        TabItem.Settings,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFEEEEEE),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .background(Color(0xFF83E3BD))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                TabIcon(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    profileBitmap = profileBitmap, // ← 프로필 이미지 전달
                    context = context,
                    token = token,
                    onClick = {
                        if (tab == TabItem.Call) {
                            onPhoneClick()
                        } else {
                            onTabSelected(tab)
                        }
                    }
                )
            }
        }
    }
}

sealed class TabItem(val title: String, val icon: @Composable (Bitmap?, Context, String?) -> Unit) {
    object Profile : TabItem("프로필", { profileBitmap, context, token ->
        // 🔧 실제 프로필 이미지 로직 적용
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (!token.isNullOrBlank()) {
            // API에서 이미지 로드
            val request = ImageRequest.Builder(context)
                .data(RetrofitClient.getImageUrl("user/profile-image"))
                .addHeader("Authorization", "Bearer $token")
                .crossfade(true)
                .build()
            AsyncImage(
                model = request,
                contentDescription = "프로필 이미지",
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )
        } else {
            // 기본 이미지
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    })

    object Call : TabItem("전화", { _, _, _ ->
        Icon(Icons.Default.Call, contentDescription = "전화")
    })
    object Home : TabItem("홈", { _, _, _ ->
        Icon(Icons.Default.Home, contentDescription = "홈")
    })
    object Chat : TabItem("채팅", { _, _, _ ->
        Icon(Icons.Default.Person, contentDescription = "채팅")
    })
    object Settings : TabItem("설정", { _, _, _ ->
        Icon(Icons.Default.Settings, contentDescription = "설정")
    })
}

@Composable
fun TabIcon(
    tab: TabItem,
    isSelected: Boolean,
    profileBitmap: Bitmap? = null,
    context: Context,
    token: String?,
    onClick: () -> Unit
) {
    val tintColor = if (isSelected) Color(0xFFFFFFFF) else Color.Gray

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides tintColor) {
            tab.icon(profileBitmap, context, token) // ← 매개변수 전달
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.title,
            fontSize = 12.sp,
            color = tintColor
        )
    }
}
