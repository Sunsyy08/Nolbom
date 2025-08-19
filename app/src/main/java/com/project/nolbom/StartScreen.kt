package com.project.nolbom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun StartScreen(navController: NavController) {
    // 3초 후 자동으로 다음 화면으로 이동
    LaunchedEffect(Unit) {
        delay(1000) // 3초 대기
        navController.navigate(Screen.SignUp.route) {
            popUpTo(Screen.Start.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF8))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))  // 상단 여백

        // 상단 제목 텍스트
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AI로 지켜주는",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4C35)
            )
            Text(
                text = "따뜻한 돌봄",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4C35)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 중앙 이미지
        Image(
            painter = painterResource(id = R.drawable.start_nolbom),
            contentDescription = "보호자와 노약자",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 소개 글
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "놀봄이는 실시간 위치 추적과",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Text(
                text = "위급 상황 알림으로",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Text(
                text = "소중한 가족의 안전을 지켜주는 보호자 앱입니다.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(60.dp))  // 버튼이 있던 자리 여백
    }
}