package com.project.nolbom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SignUpExtraScreen(navController: NavController) {
    var birth by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    val genderOptions = listOf("남성", "여성")
    val selectedGender = remember { mutableStateOf("남성") }

    // 모달 표시 여부
    var showRoleDialog by remember { mutableStateOf(false) }
    // 에러 다이얼로그 표시 여부
    var showErrorDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .offset(y = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "추가 정보 입력",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 생년월일
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("생년월일", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = birth,
                    onValueChange = { birth = it },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 핸드폰 번호
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("핸드폰 번호", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
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

            // 회원 유형 선택 버튼 & 현재 선택 표시
            Text(text = "회원 유형", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
            Button(
                onClick = { showRoleDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = when(role) {
                        "ward"     -> "노약자"
                        "guardian" -> "보호자"
                        else       -> "선택하기"
                    },
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 다음 버튼
            Button(
                onClick = {
                    if (role.isBlank()) {
                        showErrorDialog = true
                    } else if (role == "ward") {
                        navController.navigate(Screen.WardSignup.route)
                    } else {
                        navController.navigate(Screen.GuardianSignup.route)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD1A5)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(top = 20.dp)
            ) {
                Text("다음", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // 회원 유형 선택 다이얼로그
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("회원 유형을 선택해주세요") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RoleOption(
                        label = "노약자",
                        drawable = R.drawable.guardian,
                        selected = role == "ward"
                    ) {
                        role = "ward"
                        showRoleDialog = false
                    }
                    RoleOption(
                        label = "보호자",
                        drawable = R.drawable.ward,
                        selected = role == "guardian"
                    ) {
                        role = "guardian"
                        showRoleDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 에러 다이얼로그
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("알림") },
            text = { Text("회원 유형을 선택해주세요.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}

@Composable
private fun RoleOption(
    label: String,
    @DrawableRes drawable: Int,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = if (selected) BorderStroke(2.dp, Color(0xFF4FD1A5)) else null
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = label,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label)
    }
}
