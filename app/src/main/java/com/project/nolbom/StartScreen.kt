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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StartScreen(navController: NavController) {
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

        Spacer(modifier = Modifier.height(60.dp))  // 버튼을 좀 위로 올림

        // 시작하기 버튼
        Button(
            onClick = { navController.navigate(Screen.SignUp.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF61D6A8)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("시작하기", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))  // 하단 여백
    }
}

