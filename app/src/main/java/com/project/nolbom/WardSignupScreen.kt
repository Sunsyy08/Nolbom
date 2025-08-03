package com.project.nolbom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WardSignupScreen(
    navController: NavController
) {
    val profileImage = painterResource(id = R.drawable.ward_profile)
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var medicalStatus by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }

    // Pinterest 스타일 배경 그라데이션
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F5F5), Color(0xFFEDEEF0))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "환영합니다!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = "노약자 전용 회원가입",
                fontSize = 18.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 카드 스타일 컨테이너
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { /* 카메라 열기 */ }
                    ) {
                        Image(
                            painter = profileImage,
                            contentDescription = "프로필 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { /* 카메라 열기 */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "카메라",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 입력 필드들 (키)
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("키 (cm)") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 입력 필드들 (몸무게)
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("몸무게 (kg)") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 의학 상태 입력
                    OutlinedTextField(
                        value = medicalStatus,
                        onValueChange = { medicalStatus = it },
                        label = { Text("현재 의학 상태") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 집 주소 입력
                    OutlinedTextField(
                        value = homeAddress,
                        onValueChange = { homeAddress = it },
                        label = { Text("집 주소") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* 가입 완료 후 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("시작하기", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
