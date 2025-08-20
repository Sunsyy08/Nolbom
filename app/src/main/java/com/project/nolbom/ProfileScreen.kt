@file:OptIn(ExperimentalMaterial3Api::class)

package com.project.nolbom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.nolbom.data.model.ProfileUserData
import com.project.nolbom.data.repository.ProfileRepository
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.network.RetrofitClient

@Composable
fun ProfileScreen(
    navController: NavController,
    onEditProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        ProfileViewModel(ProfileRepository(context))
    }
    val uiState by viewModel.uiState.collectAsState()

    // ✅ 화면 진입 시 1회 자동 로드
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    // 에러 처리
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // 헤더
        TopAppBar(
            title = {
                Text(
                    text = "프로필",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            actions = {
                IconButton(onClick = { viewModel.loadProfile() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "새로고침"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // 컨텐츠
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.profile != null -> {
                ProfileContent(
                    profile = uiState.profile!!,
                    onEditProfile = onEditProfile,
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "프로필을 불러올 수 없습니다",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: ProfileUserData,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // 프로필 카드
        ProfileCard(profile = profile)

        Spacer(modifier = Modifier.height(24.dp))

        // 정보 카드들
        ProfileInfoSection(profile = profile)

        Spacer(modifier = Modifier.height(32.dp))

        // 편집 버튼
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = "프로필 편집",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        // 하단 여백 (탭바 공간)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProfileCard(profile: ProfileUserData) {
    val context = LocalContext.current
    val isWard = profile.userType == "노약자"
    val token = TokenStore.getToken()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 프로필 이미지 (루트 A) ---
            if (isWard && !token.isNullOrBlank()) {
                val request = ImageRequest.Builder(context)
                    .data(RetrofitClient.getImageUrl("user/profile-image"))
                    .addHeader("Authorization", "Bearer $token")
                    .crossfade(true)
                    .build()
                AsyncImage(
                    model = request,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else if (profile.profileImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profile.profileImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "기본 프로필 이미지",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            // --- 여기부터 '이름' 과 '배지' 다시 추가 ---
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profile.name.ifBlank { "이름 미등록" }, // 혹시 공백일 경우 대비
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            UserTypeBadge(userType = profile.userType)
        }
    }
}


@Composable
fun ProfileInfoSection(profile: ProfileUserData) {
    Column {
        // 이메일 (항상 표시)
        ProfileInfoCard(
            icon = Icons.Default.Email,
            iconColor = Color(0xFF2563EB),
            backgroundColor = Color(0xFFDBEAFE),
            label = "이메일",
            value = profile.email
        )

        // 🔧 생년월일 - if 문 사용으로 수정
        if (profile.birthDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoCard(
                icon = Icons.Default.DateRange,
                iconColor = Color(0xFF059669),
                backgroundColor = Color(0xFFD1FAE5),
                label = "생년월일",
                value = profile.birthDate
            )
        }

        // 🔧 전화번호 - if 문 사용으로 수정
        if (profile.phoneNumber != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoCard(
                icon = Icons.Default.Phone,
                iconColor = Color(0xFF7C3AED),
                backgroundColor = Color(0xFFE9D5FF),
                label = "전화번호",
                value = profile.phoneNumber
            )
        }

        // 🔧 주소 - if 문 사용으로 수정
        if (profile.address != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoCard(
                icon = Icons.Default.LocationOn,
                iconColor = Color(0xFFEA580C),
                backgroundColor = Color(0xFFFED7AA),
                label = "주소",
                value = profile.address
            )
        }
    }
}

@Composable
fun UserTypeBadge(userType: String) {
    val (backgroundColor, textColor, icon) = when (userType) {
        "노약자" -> Triple(
            Color(0xFFFCE7F3),
            Color(0xFFBE185D),
            Icons.Default.Favorite
        )
        else -> Triple(
            Color(0xFFDBEAFE),
            Color(0xFF1D4ED8),
            Icons.Default.Shield
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = userType,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = userType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827),
                    lineHeight = 24.sp
                )
            }
        }
    }
}