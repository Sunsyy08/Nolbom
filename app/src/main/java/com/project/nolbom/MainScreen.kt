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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.accompanist.flowlayout.FlowRow

// API 연동을 위한 추가 imports
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavHostController

// 올바른 import 경로들
import com.project.nolbom.data.model.UserProfile
import com.project.nolbom.data.repository.UserRepository

// 전화 앱 실행을 위한 함수
fun openPhoneApp(context: Context) {
    try {
        // 전화 다이얼러를 열기 (번호 입력 화면)
        val intent = Intent(Intent.ACTION_DIAL)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 특정 번호로 전화를 거는 함수 (옵션)
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

@Composable
fun MainScreen(
    navController: NavHostController,
    onNavigateToAlertList: () -> Unit

) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ViewModel 생성을 정확한 Composable 문맥에서 실행
    val viewModel: MainViewModel = viewModel {
        MainViewModel(UserRepository(context))
    }

    val uiState by viewModel.uiState.collectAsState()

    // JSON에서 사용자 리스트 읽기
    val userList = remember {
        loadUsersFromAssets(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                            onClick = { viewModel.retryLoadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF83E3BD))
                        ) {
                            Text("다시 시도")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 🆕 테스트용 데이터 초기화 버튼
                        Button(
                            onClick = { viewModel.clearUserData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("데이터 초기화")
                        }
                    }
                }
            }
        }

        // 상단 프로필 헤더 (실제 데이터 사용)
        ProfileHeaderWithData(
            userProfile = uiState.userProfile,
            profileBitmap = uiState.profileBitmap
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 중간 영역: 왼쪽 리스트, 오른쪽 지도
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(400.dp)
        ) {
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                MiniMapView(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ActionCardSection(onNavigateToAlertList)
        Spacer(modifier = Modifier.weight(1f))
        // 전화 앱 실행 함수를 전달
        BottomTabBar(
            onPhoneClick = { openPhoneApp(context) },
            onTabSelected = { tab ->
                when (tab) {
                    TabItem.Profile -> navController.navigate(Screen.Profile.route) // ← 프로필 이동
                    else -> { /* 다른 탭 동작 */ }
                }
            }
        )
    }
}

// 실제 데이터를 사용하는 프로필 헤더
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
        // 프로필 이미지 (실제 데이터 또는 기본 이미지)
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

        // 사용자 이름 + 주소 (실제 데이터 또는 기본값)
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

@Composable
fun ProfileHeader(userName: String, userRegion: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userRegion,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp)
            )
        }
    }
}

@Composable
fun AlertCardSmall(user: AlertUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "사용자 프로필",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

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
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
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
    onTabSelected: (TabItem) -> Unit = {},
    selectedTab: TabItem = TabItem.Home,
    onPhoneClick: () -> Unit = {} // 전화 클릭 콜백 추가
) {
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
                    onClick = {
                        if (tab == TabItem.Call) {
                            onPhoneClick() // 전화 탭 클릭시 전화 앱 실행
                        } else {
                            onTabSelected(tab)
                        }
                    }
                )
            }
        }
    }
}

sealed class TabItem(val title: String, val icon: @Composable () -> Unit) {
    object Profile : TabItem("프로필", {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "프로필",
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    })
    object Call : TabItem("전화", { Icon(Icons.Default.Call, contentDescription = "전화") })
    object Home : TabItem("홈", { Icon(Icons.Default.Home, contentDescription = "홈") })
    object Chat : TabItem("채팅", { Icon(Icons.Default.Person, contentDescription = "채팅") })
    object Settings : TabItem("설정", { Icon(Icons.Default.Settings, contentDescription = "설정") })
}

@Composable
fun TabIcon(tab: TabItem, isSelected: Boolean, onClick: () -> Unit) {
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
            tab.icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.title,
            fontSize = 12.sp,
            color = tintColor
        )
    }
}