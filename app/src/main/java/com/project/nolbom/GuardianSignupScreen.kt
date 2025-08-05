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
import kotlinx.coroutines.launch

@Composable
fun GuardianSignupScreen(
    userId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    var wardEmail by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFAAF0D1), Color(0xFFB2EBF2))
                    )
                ),
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

        // 하단 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = wardEmail,
                    onValueChange = { wardEmail = it },
                    label = { Text("노약자 이메일") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                    label = { Text("집 주소") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                    label = { Text("노약자와의 관계") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                                        val req = GuardianSignupRequest(
                                            wardEmail = wardEmail.trim(),
                                            address = address.trim(),
                                            relation = relation.trim()
                                        )
                                        val resp = RetrofitClient.api.signupGuardian(userId, req)
                                        if (resp.success) {
                                            navController.navigate(Screen.Main.route) {
                                                popUpTo(Screen.GuardianSignup.route) { inclusive = true }
                                            }
                                        } else {
                                            errorMessage = resp.message
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
                            if (errorMessage.isNotBlank() && showErrorDialog.not()) showErrorDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Text("시작하기", fontSize = 16.sp, color = Color.White)
                }

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
        }
    }
}
