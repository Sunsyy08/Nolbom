package com.project.nolbom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.project.nolbom.utils.VoiceRecorder
import com.project.nolbom.utils.RequestAudioPermission
import com.project.nolbom.utils.hasAudioPermission
import com.project.nolbom.data.repository.STTRepository

@Composable
fun VoiceTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var isRecording by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val voiceRecorder = remember { VoiceRecorder(context) }
    val sttRepository = remember { STTRepository() }

    RequestAudioPermission(
        onPermissionGranted = {
            hasPermission = true
            println("✅ 음성 권한 허용됨")
        },
        onPermissionDenied = {
            hasPermission = false
            errorMessage = "음성 녹음 권한이 필요합니다"
            showError = true
        }
    ) { requestPermission ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 제목
            Text(
                text = "음성 인식 테스트",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 권한 상태 표시
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPermission) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasPermission) "✅ 음성 권한 허용됨" else "❌ 음성 권한 필요함",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 권한 요청 버튼
            if (!hasPermission) {
                Button(
                    onClick = { requestPermission() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("음성 권한 허용하기", fontWeight = FontWeight.Bold)
                }
            }

            // 음성 녹음 버튼 (권한이 있을 때만)
            if (hasPermission) {
                Button(
                    onClick = {
                        if (!isRecording && !isProcessing) {
                            // 3초 녹음 시작
                            isProcessing = true
                            scope.launch {
                                try {
                                    println("🎤 3초 음성 녹음 시작...")
                                    recognizedText = "🎤 녹음 중... (3초)"

                                    val audioBase64 = voiceRecorder.recordShortAudio(3000)

                                    if (audioBase64 != null) {
                                        recognizedText = "🔄 음성 인식 중..."

                                        sttRepository.recognizeVoice(audioBase64).fold(
                                            onSuccess = { response ->
                                                recognizedText = "📝 인식된 텍스트: ${response.transcript}"
                                                if (response.keywordDetected) {
                                                    recognizedText += "\n🚨 키워드 감지됨!"
                                                    if (response.smsSent) {
                                                        recognizedText += "\n📱 SMS 전송됨!"
                                                    }
                                                }
                                            },
                                            onFailure = { error ->
                                                errorMessage = "음성 인식 실패: ${error.message}"
                                                showError = true
                                                recognizedText = ""
                                            }
                                        )
                                    } else {
                                        errorMessage = "음성 녹음에 실패했습니다"
                                        showError = true
                                        recognizedText = ""
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "오류: ${e.message}"
                                    showError = true
                                    recognizedText = ""
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .size(120.dp),
                    shape = RoundedCornerShape(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isProcessing) Color.Gray else Color(0xFFE91E63)
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "음성 녹음",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isProcessing) "처리 중..." else "3초 음성 녹음",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 인식 결과 표시
            if (recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text(
                        text = recognizedText,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // 에러 다이얼로그
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("오류") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("확인")
                }
            }
        )
    }

    // 컴포저블이 종료될 때 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            voiceRecorder.release()
        }
    }
}