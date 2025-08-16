// AlertListScreen.kt - 백엔드 연동 버전 (기존 UI 유지)
package com.project.nolbom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.nolbom.list.AlertUser
import com.project.nolbom.AlertListViewModel

@Composable
fun AlertListScreen(
    viewModel: AlertListViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val alertUsers by viewModel.alertUsers.collectAsState()

    // 🆕 백엔드 연결 실패 시 JSON 로드 (기존 방식 유지)
    val fallbackUsers = remember { loadUsersFromAssets(context) }

    // 🆕 실제로 표시할 사용자 목록 (백엔드 우선, 실패시 JSON)
    val displayUsers = if (uiState.isConnectedToBackend) alertUsers else fallbackUsers

    // 스낵바 표시
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 🆕 헤더에 연결 상태 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "안내 문자 대상자 목록",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 🆕 연결 상태 표시
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isConnectedToBackend)
                                Icons.Default.Cloud else Icons.Default.CloudOff,
                            contentDescription = "연결 상태",
                            modifier = Modifier.size(16.dp),
                            tint = if (uiState.isConnectedToBackend) Color.Green else Color.Gray
                        )
                        Text(
                            text = if (uiState.isConnectedToBackend)
                                "실시간 업데이트 (${uiState.totalCount}명)"
                            else "오프라인 모드 (${displayUsers.size}명)",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // 🆕 새로고침 버튼
                IconButton(
                    onClick = { viewModel.refresh() },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🆕 로딩 상태 표시
            if (uiState.isLoading && displayUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF83E3BD))
                        Text(
                            text = "실종자 목록을 불러오는 중...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // 사용자 목록 표시 (기존 UI 유지)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayUsers) { user ->
                        AlertCardLarge(user)
                    }

                    // 🆕 빈 상태 표시
                    if (displayUsers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                isConnectedToBackend = uiState.isConnectedToBackend,
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🆕 빈 상태 카드
@Composable
fun EmptyStateCard(
    isConnectedToBackend: Boolean,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isConnectedToBackend)
                    Icons.Default.PersonSearch else Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )

            Text(
                text = if (isConnectedToBackend)
                    "현재 실종자가 없습니다"
                else "서버에 연결할 수 없습니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Text(
                text = if (isConnectedToBackend)
                    "모든 분들이 안전하게 계십니다"
                else "네트워크 연결을 확인해주세요",
                fontSize = 14.sp,
                color = Color.Gray
            )

            if (!isConnectedToBackend) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF83E3BD)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("다시 시도")
                }
            }
        }
    }
}

// 기존 AlertCardLarge 함수 그대로 유지
@Composable
fun AlertCardLarge(user: AlertUser) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF83E3BD))
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "나이: ${user.age}", fontSize = 14.sp, color = Color.DarkGray)
                Text(text = "신장/체중: ${user.height} / ${user.weight}", fontSize = 14.sp, color = Color.DarkGray)
                Text(text = "위치: ${user.location}", fontSize = 14.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 지도 (기존과 동일)
            MiniMapView(
                modifier = Modifier
                    .width(110.dp)
                    .height(120.dp)
            )
        }
    }
}