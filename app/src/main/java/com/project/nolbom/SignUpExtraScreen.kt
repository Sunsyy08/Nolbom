package com.project.nolbom.ui.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.runtime.rememberCoroutineScope
import com.project.nolbom.Screen
import com.project.nolbom.data.model.SignupExtraRequest
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.launch
import com.project.nolbom.R
import com.project.nolbom.data.repository.STTRepository
import com.project.nolbom.data.local.TokenStore

@Composable
fun SignUpExtraScreen(
    userId: Long,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    var birthState by remember { mutableStateOf(TextFieldValue("")) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    val genderOptions = listOf("남성", "여성")
    var selectedGender by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    var showRoleDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5FBFA),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        // 떠다니는 원들 - 이전 화면과 다른 위치
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    Color(0x184FD1A5),
                    CircleShape
                )
                .blur(25.dp)
                .align(Alignment.TopCenter)
        )

        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Color(0x2261D6A8),
                    CircleShape
                )
                .blur(18.dp)
                .align(Alignment.TopStart)
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    Color(0x154CAF50),
                    CircleShape
                )
                .blur(22.dp)
                .align(Alignment.CenterEnd)
        )

        Box(
            modifier = Modifier
                .size(75.dp)
                .background(
                    Color(0x284FD1A5),
                    CircleShape
                )
                .blur(16.dp)
                .align(Alignment.BottomStart)
        )

        Box(
            modifier = Modifier
                .size(95.dp)
                .background(
                    Color(0x1A61D6A8),
                    CircleShape
                )
                .blur(20.dp)
                .align(Alignment.BottomEnd)
        )

        Box(
            modifier = Modifier
                .size(65.dp)
                .background(
                    Color(0x304CAF50),
                    CircleShape
                )
                .blur(14.dp)
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "추가 정보 입력",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 생년월일 입력
            OutlinedTextField(
                value = birthState,
                onValueChange = { newValue ->
                    val digits = newValue.text.filter { it.isDigit() }.take(8)
                    val year = digits.take(4)
                    val month = digits.drop(4).take(2)
                    val day = digits.drop(6).take(2)
                    val formatted = buildString {
                        append(year)
                        if (month.isNotEmpty()) append("-").append(month)
                        if (day.isNotEmpty()) append("-").append(day)
                    }
                    birthState = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                label = { Text("생년월일 (YYYY-MM-DD)", color = Color(0xFF666666)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF4FD1A5)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4FD1A5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFF4FD1A5),
                    cursorColor = Color(0xFF4FD1A5)
                )
            )

            // 핸드폰 번호 입력
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
                label = { Text("핸드폰 번호 (010-1234-5678)", color = Color(0xFF666666)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4FD1A5)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4FD1A5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFF4FD1A5),
                    cursorColor = Color(0xFF4FD1A5)
                )
            )

            // 성별 선택
            Text(
                text = "성별 선택",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2E2E),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                genderOptions.forEach { gender ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedGender == gender) Color(0xFF4FD1A5) else Color(0xFFF5F5F5),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
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
                                color = if (selectedGender == gender) Color.White else Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            // 회원 유형 선택
            Text(
                text = "회원 유형",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF2E2E2E),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp)
            )
            Button(
                onClick = { showRoleDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role.isNotBlank()) Color(0xFF4FD1A5) else Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = when(role) {
                        "ward"     -> "노약자"
                        "guardian" -> "보호자"
                        else       -> "선택하기"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (role.isNotBlank()) Color.White else Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 다음 버튼
            Button(
                onClick = {
                    when {
                        birthState.text.isBlank() -> {
                            errorMessage = "생년월일을 입력해주세요"
                            showErrorDialog = true
                        }
                        phoneState.text.isBlank() -> {
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
                            isLoading = true
                            scope.launch {
                                try {
                                    // 1. 추가 정보 저장
                                    val req = SignupExtraRequest(
                                        birthdate = birthState.text,
                                        phone     = phoneState.text,
                                        gender    = selectedGender,
                                        role      = role
                                    )
                                    val resp = RetrofitClient.api.signupExtra(userId, req)
                                    if (!resp.success) throw Exception(resp.message)

                                    // 2. 업데이트된 토큰 저장
                                    if (!resp.token.isNullOrEmpty()) {
                                        TokenStore.saveToken(resp.token)
                                    } else {
                                        throw Exception("토큰을 받지 못했습니다")
                                    }

                                    // 3. STT 자동 활성화 시도
                                    var sttActivationMessage = ""
                                    try {
                                        val sttRepository = STTRepository()
                                        val sttResult = sttRepository.activateSTT(enable = true)

                                        sttResult.fold(
                                            onSuccess = { sttResponse ->
                                                if (sttResponse.success) {
                                                    sttActivationMessage = "\n🎤 음성 모니터링이 활성화되었습니다!"
                                                } else {
                                                    sttActivationMessage = "\n음성 모니터링은 메인 화면에서 수동으로 활성화해주세요."
                                                }
                                            },
                                            onFailure = {
                                                sttActivationMessage = "\n음성 모니터링은 메인 화면에서 활성화해주세요."
                                                println("STT 활성화 실패: ${it.message}")
                                            }
                                        )
                                    } catch (sttError: Exception) {
                                        sttActivationMessage = "\n음성 모니터링은 메인 화면에서 활성화해주세요."
                                        println("STT 활성화 중 오류: ${sttError.message}")
                                    }

                                    // 4. 성공 메시지와 함께 성공 다이얼로그 표시
                                    successMessage = "회원가입이 완료되었습니다!$sttActivationMessage"
                                    showSuccessDialog = true

                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "추가 정보 저장에 실패했습니다"
                                    showErrorDialog = true
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FD1A5),
                    disabledContainerColor = Color(0xFF4FD1A5).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Text("처리 중...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "가입 완료",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // 성공 다이얼로그
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {},  // 버튼으로만 닫기
                    title = {
                        Text(
                            "가입 완료!",
                            color = Color(0xFF4FD1A5),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = { Text(successMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSuccessDialog = false
                                // role에 따라 다음 화면으로 이동
                                if (role == "ward") {
                                    navController.navigate(Screen.WardSignup.createRoute(userId)) {
                                        popUpTo(Screen.SignUpExtra.route) { inclusive = true }
                                    }
                                } else if (role == "guardian") {
                                    navController.navigate(Screen.GuardianSignup.createRoute(userId)) {
                                        popUpTo(Screen.SignUpExtra.route) { inclusive = true }
                                    }
                                } else {
                                    // 메인 화면으로 이동 (추후 구현)
                                    navController.navigate("main") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        ) {
                            Text("확인", fontWeight = FontWeight.Bold, color = Color(0xFF4FD1A5))
                        }
                    }
                )
            }
        }
    }

    // 회원 유형 다이얼로그
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = {
                Text(
                    "회원 유형을 선택해주세요",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E)
                )
            },
            text = {
                Box {
                    // 다이얼로그용 떠다니는 원들
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Color(0x204FD1A5),
                                CircleShape
                            )
                            .blur(12.dp)
                            .align(Alignment.TopStart)
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0x3061D6A8),
                                CircleShape
                            )
                            .blur(8.dp)
                            .align(Alignment.TopEnd)
                    )

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Color(0x154CAF50),
                                CircleShape
                            )
                            .blur(10.dp)
                            .align(Alignment.BottomCenter)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RoleOption("노약자", R.drawable.guardian, role == "ward") {
                            role = "ward"
                            showRoleDialog = false
                        }
                        RoleOption("보호자", R.drawable.ward, role == "guardian") {
                            role = "guardian"
                            showRoleDialog = false
                        }
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
                    Text("확인", color = Color(0xFF4FD1A5))
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
            shape = RoundedCornerShape(16.dp),
            color = if (selected) Color(0xFF4FD1A5) else Color(0xFFF5F5F5),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = drawable),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color(0xFF4FD1A5) else Color(0xFF666666)
        )
    }
}