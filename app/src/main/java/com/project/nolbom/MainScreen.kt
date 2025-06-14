package com.project.nolbom

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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


fun loadUsersFromAssets(context: Context): List<AlertUser> {
    val jsonString = context.assets.open("user.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val type = object : TypeToken<List<AlertUser>>() {}.type
    return gson.fromJson(jsonString, type)
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // JSON에서 사용자 리스트 읽기 (Compose 재구성 시마다 불필요한 호출 막으려 remember로 감싸기)
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
        // 상단 프로필 헤더
        ProfileHeader(
            userName = "김철수",
            userRegion = "서울 은평구"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 중간 영역: 왼쪽 리스트, 오른쪽 지도 (가로 분할)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(400.dp)
        ) {
            // 🟢 리스트 영역 Box로 감싸고 배경색 지정
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF83E3BD), RoundedCornerShape(20.dp)) // 💚 이 색!
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

            // 지도 영역
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
        ActionCardSection()
        Spacer(modifier = Modifier.weight(1f)) // 내용 밀어올림
        BottomTabBar()
        // 여기에 버튼 등 추가 가능
    }
}


@Composable
fun ProfileHeader(userName: String, userRegion: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // 프로필 이미지 (왼쪽)
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 사용자 이름 + 지역 (오른쪽)
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
fun ActionCardSection() {
    // Row 전체를 감싸는 카드 배경
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        // 원하는 카드 배경색 지정
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF83E3BD), RoundedCornerShape(12.dp))
                .padding(12.dp), // 카드 안쪽 패딩
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
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
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
                .fillMaxSize()        // 내부 전체를 차지해서 가운데 정렬이 정확하게 됨
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
    selectedTab: TabItem = TabItem.Home
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
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

sealed class TabItem(val title: String, val icon: @Composable () -> Unit) {
    object Profile : TabItem("프로필", {
        // 프로필 이미지를 아이콘처럼
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
    object Chat : TabItem("채팅", { Icon(Icons.Default.Person, contentDescription = "채팅") }) // 사람 아이콘
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
