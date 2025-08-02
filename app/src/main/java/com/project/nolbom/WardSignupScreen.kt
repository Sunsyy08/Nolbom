package com.project.nolbom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WardSignupScreen(
    navController: NavController
) {
    val profileImage = painterResource(id = R.drawable.ward) // 예시 이미지
    val genderOptions = listOf("남성", "여성")
    val selectedGender = remember { mutableStateOf("남성") }
    val medicalStatus = remember { mutableStateOf("") }
    val homeAddress = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            // 프로필 이미지 박스
            Box(modifier = Modifier.size(120.dp)) {
                Image(
                    painter = profileImage,
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
                IconButton(
                    onClick = { /* 카메라 열기 구현 */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "카메라",
                        tint = Color.Gray
                    )
                }
            }

            // 성별 선택
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                genderOptions.forEach { gender ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGender.value == gender,
                            onClick = { selectedGender.value = gender }
                        )
                        Text(text = gender)
                    }
                }
            }

            // 의학 상태 입력
            OutlinedTextField(
                value = medicalStatus.value,
                onValueChange = { medicalStatus.value = it },
                label = { Text("현재 의학 상태") },
                modifier = Modifier.fillMaxWidth()
            )

            // 집 주소 입력
            OutlinedTextField(
                value = homeAddress.value,
                onValueChange = { homeAddress.value = it },
                label = { Text("집 주소") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 바로 시작하기 버튼
        Button(
            onClick = {

            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("바로 시작하기", color = Color.White, fontSize = 16.sp)
        }
    }
}
