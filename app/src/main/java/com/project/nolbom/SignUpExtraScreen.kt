package com.project.nolbom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.min


@Composable
fun SignUpExtraScreen(navController: NavController) {
    var birth by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthState by remember { mutableStateOf(TextFieldValue("")) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    var role by remember { mutableStateOf("") }
    val genderOptions = listOf("남성", "여성")
    var selectedGender by remember { mutableStateOf("") }

    var showRoleDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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

            // 생년월일 입력 (YYYY-MM-DD)
            OutlinedTextField(
                value = birthState,
                onValueChange = { newValue ->
                    // 입력된 전체 텍스트
                    val digits = newValue.text.filter { it.isDigit() }
                    val year = digits.take(4)
                    val month = digits.drop(4).take(2)
                    val day = digits.drop(6).take(2)

                    // 포맷된 새 문자열
                    val formatted = buildString {
                        append(year)
                        if (month.isNotEmpty()) append("-").append(month)
                        if (day.isNotEmpty()) append("-").append(day)
                    }

                    // 커서를 항상 새 텍스트 맨 뒤로
                    birthState = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                label = { Text("생년월일") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF4CAF50)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // 핸드폰 번호 입력 (XXX-XXXX-XXXX)
            OutlinedTextField(
                value = phoneState,
                onValueChange = { newValue ->
                    val digits = newValue.text.filter { it.isDigit() }.take(11)
                    val part1 = digits.take(3)
                    val part2 = digits.drop(3).take(4)
                    val part3 = digits.drop(7).take(4)

                    val formatted = buildString {
                        append(part1)
                        if (part2.isNotEmpty()) append("-").append(part2)
                        if (part3.isNotEmpty()) append("-").append(part3)
                    }

                    phoneState = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                label = { Text("핸드폰 번호") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4CAF50)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // ─── 성별 선택 ───
            Text(
                text = "성별 선택",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                genderOptions.forEach { gender ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedGender == gender) Color(0xFF4FD1A5) else Color(0xFFE0E0E0),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { selectedGender = gender }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = gender,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedGender == gender) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            // 회원 유형 선택 버튼
            Text(
                text = "회원 유형",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            Button(
                onClick = { showRoleDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role.isNotBlank()) Color(0xFF4FD1A5) else Color(0xFFE0E0E0)
                ),
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
                    color = if (role.isNotBlank()) Color.White else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 다음 버튼 ───
            Button(
                onClick = {
                    when {
                        birth.isBlank() -> {
                            errorMessage = "생년월일을 입력해주세요"
                            showErrorDialog = true
                        }
                        phone.isBlank() -> {
                            errorMessage = "핸드폰 번호를 입력해주세요"
                            showErrorDialog = true
                        }
                        selectedGender.isBlank() -> {
                            errorMessage = "성별을 선택해주세요"
                            showErrorDialog = true
                        }
                        role.isBlank() -> {
                            errorMessage = "회원 유형을 선택해주세요"
                            showErrorDialog = true
                        }
                        else -> {
                            if (role == "ward")
                                navController.navigate(Screen.WardSignup.route)
                            else
                                navController.navigate(Screen.GuardianSignup.route)
                        }
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
            title = { Text("입력 오류") },
            text = { Text(errorMessage) },
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
            .padding(8.dp)
            .clickable(onClick = onSelect)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selected) Color(0xFF4FD1A5) else Color(0xFFE0E0E0),
            modifier = Modifier.size(120.dp)  // ← 여기서 사이즈를 키웁니다
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(drawable),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color.Black
        )
    }
}

