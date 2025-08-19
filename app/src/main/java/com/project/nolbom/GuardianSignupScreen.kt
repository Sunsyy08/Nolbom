package com.project.nolbom

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.nolbom.R
import com.project.nolbom.data.model.GuardianSignupRequest
import com.project.nolbom.data.network.RetrofitClient
import com.project.nolbom.data.repository.SignupRepository
import kotlinx.coroutines.launch

@Composable
fun GuardianSignupScreen(
    userId: Long,
    navController: NavController,
    userEmail: String = "",
    userName: String = ""
) {
    val context = LocalContext.current
    var wardEmail by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val signupRepository = remember { SignupRepository(context = context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFAAF0D1), Color(0xFFB2EBF2))
                )
            )
    ) {
        // 떠다니는 원들 - 보호자 화면용 다른 배치
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    Color(0x30FFFFFF),
                    CircleShape
                )
                .blur(24.dp)
                .align(Alignment.TopStart)
        )

        Box(
            modifier = Modifier
                .size(85.dp)
                .background(
                    Color(0x35FFFFFF),
                    CircleShape
                )
                .blur(16.dp)
                .align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .size(105.dp)
                .background(
                    Color(0x25FFFFFF),
                    CircleShape
                )
                .blur(20.dp)
                .align(Alignment.CenterStart)
        )

        Box(
            modifier = Modifier
                .size(75.dp)
                .background(
                    Color(0x40FFFFFF),
                    CircleShape
                )
                .blur(15.dp)
                .align(Alignment.CenterEnd)
        )

        Box(
            modifier = Modifier
                .size(130.dp)
                .background(
                    Color(0x28FFFFFF),
                    CircleShape
                )
                .blur(22.dp)
                .align(Alignment.BottomStart)
        )

        Box(
            modifier = Modifier
                .size(95.dp)
                .background(
                    Color(0x33FFFFFF),
                    CircleShape
                )
                .blur(18.dp)
                .align(Alignment.BottomEnd)
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    Color(0x38FFFFFF),
                    CircleShape
                )
                .blur(12.dp)
                .align(Alignment.Center)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "보호자 회원가입",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "노약자와 연결하세요",
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // 하단 폼 섹션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = wardEmail,
                        onValueChange = { wardEmail = it },
                        label = { Text("노약자 이메일", color = Color(0xFF666666)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF4FD1A5),
                            focusedLabelColor = Color(0xFF4FD1A5),
                            cursorColor = Color(0xFF4FD1A5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("집 주소", color = Color(0xFF666666)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF4FD1A5),
                            focusedLabelColor = Color(0xFF4FD1A5),
                            cursorColor = Color(0xFF4FD1A5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("노약자와의 관계", color = Color(0xFF666666)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF4FD1A5),
                            focusedLabelColor = Color(0xFF4FD1A5),
                            cursorColor = Color(0xFF4FD1A5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                when {
                                    wardEmail.isBlank() -> errorMessage = "노약자 이메일을 입력해주세요"
                                    address.isBlank() -> errorMessage = "집 주소를 입력해주세요"
                                    relation.isBlank() -> errorMessage = "관계 정보를 입력해주세요"
                                    else -> {
                                        isLoading = true
                                        try {
                                            val result = signupRepository.completeGuardianSignup(
                                                userId = userId,
                                                wardEmail = wardEmail.trim(),
                                                address = address.trim(),
                                                relation = relation.trim(),
                                                userEmail = userEmail,
                                                userName = userName
                                            )

                                            result.onSuccess { successMessage ->
                                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                navController.navigate(Screen.Main.route) {
                                                    popUpTo(Screen.GuardianSignup.route) { inclusive = true }
                                                }
                                            }.onFailure { exception ->
                                                errorMessage = exception.message ?: "회원가입 실패"
                                                showErrorDialog = true
                                            }

                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage ?: "오류가 발생했습니다"
                                            showErrorDialog = true
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                                if (errorMessage.isNotBlank() && !showErrorDialog) {
                                    showErrorDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4FD1A5),
                            disabledContainerColor = Color(0xFF4FD1A5).copy(alpha = 0.6f)
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "시작하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showErrorDialog = false
                                errorMessage = ""
                            },
                            title = { Text("회원가입 오류") },
                            text = { Text(errorMessage) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showErrorDialog = false
                                    errorMessage = ""
                                }) {
                                    Text("확인", color = Color(0xFF4FD1A5))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}