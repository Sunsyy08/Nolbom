package com.project.nolbom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.model.UserSignupRequest
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController) {

    // 1) coroutine scope
    val scope = rememberCoroutineScope()
    // 2) form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // 에러 다이얼로그용 상태
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FDFC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        // 떠다니는 원들
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color(0x1A4FD1A5),
                    CircleShape
                )
                .blur(20.dp)
                .align(Alignment.TopStart)
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color(0x254CAF50),
                    CircleShape
                )
                .blur(15.dp)
                .align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    Color(0x2061D6A8),
                    CircleShape
                )
                .blur(12.dp)
                .align(Alignment.CenterStart)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Color(0x154FD1A5),
                    CircleShape
                )
                .blur(25.dp)
                .align(Alignment.BottomEnd)
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    Color(0x204CAF50),
                    CircleShape
                )
                .blur(18.dp)
                .align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "회원가입",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E)
            )
            Text(
                text = "환영합니다!",
                fontSize = 20.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 이름 입력
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름", color = Color(0xFF666666)) },
                leadingIcon = {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF4FD1A5))
                },
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

            // 아이디 입력
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("아이디", color = Color(0xFF666666)) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4FD1A5))
                },
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

            // 비밀번호 입력란 (눈 아이콘으로 토글)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호", color = Color(0xFF666666)) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4FD1A5))
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(mask = '*'),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF999999)
                        )
                    }
                },
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

            // 가입하기 버튼
            Button(
                onClick = {
                    // 4) validation
                    when {
                        name.isBlank() -> {
                            errorMessage = "이름을 입력해주세요"
                            showErrorDialog = true
                        }
                        email.isBlank() -> {
                            errorMessage = "아이디를 입력해주세요"
                            showErrorDialog = true
                        }
                        password.isBlank() -> {
                            errorMessage = "비밀번호를 입력해주세요"
                            showErrorDialog = true
                        }
                        else -> {
                            // 5) call signup API
                            isLoading = true
                            scope.launch {
                                try {
                                    val req = UserSignupRequest(
                                        name = name,
                                        email = email,
                                        password = password
                                    )
                                    val resp = RetrofitClient.api.signup(req)
                                    TokenStore.saveToken(resp.token)
                                    if (!resp.success) {
                                        throw Exception("회원가입에 실패했습니다")
                                    } // 생성된 유저 ID를 꺼내서
                                    val newUserId = resp.user_id
                                    // 성공 시 다음 화면으로 이동
                                    navController.navigate(Screen.SignUpExtra.createRoute(newUserId))
                                } catch (e: Exception) {
                                    // 오류 처리
                                    errorMessage = e.localizedMessage ?: "회원가입에 실패했습니다"
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
                    .height(70.dp)
                    .padding(top = 24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "가입하기",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    // 6) 에러 다이얼로그
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