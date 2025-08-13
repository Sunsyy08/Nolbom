package com.project.nolbom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.project.nolbom.utils.VoiceRecorder
import com.project.nolbom.utils.RequestAudioPermission
import com.project.nolbom.utils.hasAudioPermission
import com.project.nolbom.data.repository.STTRepository
import android.util.Log

@Composable
fun VoiceTestScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var isRecording by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 마이크 테스트 관련 상태
    var micTestResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }
    var showDebugInfo by remember { mutableStateOf(false) }

    val voiceRecorder = remember { VoiceRecorder(context) }
    val sttRepository = remember { STTRepository() }

    RequestAudioPermission(
        onPermissionGranted = {
            hasPermission = true
            Log.d("VoiceTestScreen", "✅ 음성 권한 허용됨")
        },
        onPermissionDenied = {
            hasPermission = false
            errorMessage = "음성 녹음 권한이 필요합니다"
            showError = true
            Log.e("VoiceTestScreen", "❌ 음성 권한 거부됨")
        }
    ) { requestPermission ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 제목
            Text(
                text = "음성 인식 테스트",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // 권한 상태 표시
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPermission) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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

            if (hasPermission) {
                // 마이크 테스트 섹션
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎤 마이크 테스트",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (micTestResult.isNotEmpty()) {
                            Text(
                                text = micTestResult,
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    micTestResult.startsWith("✅") -> Color(0xFF4CAF50)
                                    micTestResult.startsWith("⚠️") -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isTesting) {
                                        isTesting = true
                                        micTestResult = "테스트 중..."

                                        scope.launch {
                                            try {
                                                val result = voiceRecorder.testMicrophone()
                                                micTestResult = result
                                                Log.d("VoiceTestScreen", "마이크 테스트 결과: $result")
                                            } catch (e: Exception) {
                                                micTestResult = "❌ 테스트 실패: ${e.message}"
                                                Log.e("VoiceTestScreen", "마이크 테스트 오류", e)
                                            } finally {
                                                isTesting = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isTesting,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Mic, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTesting) "테스트 중..." else "테스트")
                            }

                            OutlinedButton(
                                onClick = { showDebugInfo = !showDebugInfo },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("디버그")
                            }
                        }
                    }
                }

                // 디버그 정보 표시
                if (showDebugInfo) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🔧 디버그 정보",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• 마이크 하드웨어 확인\n" +
                                        "• 오디오 권한 상태\n" +
                                        "• 시스템 볼륨 설정\n" +
                                        "• AudioRecord 초기화 상태",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 음성 녹음 버튼
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (!isRecording && !isProcessing) {
                                // 3초 녹음 시작
                                isProcessing = true
                                recognizedText = ""

                                scope.launch {
                                    try {
                                        Log.d("VoiceTestScreen", "🎤 3초 음성 녹음 시작...")
                                        recognizedText = "🎤 녹음 중... (3초)"

                                        val audioBase64 = voiceRecorder.recordShortAudio(3000)

                                        if (audioBase64 != null) {
                                            Log.d("VoiceTestScreen", "✅ 녹음 완료: ${audioBase64.length} chars")
                                            recognizedText = "🔄 음성 인식 중..."

                                            sttRepository.recognizeVoice(audioBase64).fold(
                                                onSuccess = { response ->
                                                    Log.d("VoiceTestScreen", "✅ 인식 성공: ${response.transcript}")
                                                    recognizedText = "📝 인식된 텍스트: ${response.transcript}"
                                                    if (response.keywordDetected) {
                                                        recognizedText += "\n🚨 키워드 감지됨!"
                                                        if (response.smsSent) {
                                                            recognizedText += "\n📱 SMS 전송됨!"
                                                        }
                                                    }
                                                },
                                                onFailure = { error ->
                                                    Log.e("VoiceTestScreen", "❌ 인식 실패: ${error.message}")
                                                    errorMessage = "음성 인식 실패: ${error.message}"
                                                    showError = true
                                                    recognizedText = ""
                                                }
                                            )
                                        } else {
                                            Log.e("VoiceTestScreen", "❌ 녹음 실패")
                                            errorMessage = "음성 녹음에 실패했습니다. 마이크를 확인해주세요."
                                            showError = true
                                            recognizedText = ""
                                        }
                                    } catch (e: Exception) {
                                        Log.e("VoiceTestScreen", "❌ 처리 오류", e)
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
                        modifier = Modifier.size(120.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isProcessing) "처리 중..." else "3초 음성 녹음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 간단한 녹음 테스트 버튼
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                Log.d("VoiceTestScreen", "🧪 간단한 녹음 테스트 시작")
                                recognizedText = "🧪 2초 테스트 녹음 중..."
                                val audioData = voiceRecorder.recordShortAudio(2000)
                                if (audioData != null) {
                                    recognizedText = "✅ 테스트 녹음 성공 (${audioData.length} chars)\n마이크가 정상 작동합니다!"
                                    Log.d("VoiceTestScreen", "✅ 테스트 녹음 성공")
                                } else {
                                    recognizedText = "❌ 테스트 녹음 실패\n마이크를 확인해주세요"
                                    Log.e("VoiceTestScreen", "❌ 테스트 녹음 실패")
                                }
                            } catch (e: Exception) {
                                recognizedText = "❌ 테스트 오류: ${e.message}"
                                Log.e("VoiceTestScreen", "테스트 녹음 오류", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.MicExternalOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2초 간단 테스트")
                }
            }

            // 인식 결과 표시
            if (recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            recognizedText.contains("✅") -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            recognizedText.contains("❌") -> Color(0xFFF44336).copy(alpha = 0.1f)
                            recognizedText.contains("🚨") -> Color(0xFFFF9800).copy(alpha = 0.1f)
                            else -> Color(0xFFF5F5F5)
                        }
                    )
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
            Log.d("VoiceTestScreen", "🧹 VoiceRecorder 리소스 정리")
        }
    }
}