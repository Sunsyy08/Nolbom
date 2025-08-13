// PermissionDebugScreen.kt - 권한 상태 확인 및 강제 요청
package com.project.nolbom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.content.ContextCompat

@Composable
fun PermissionDebugScreen() {
    val context = LocalContext.current

    // 권한 상태 추적
    var permissionStates by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val permissions = listOf(
        Manifest.permission.RECORD_AUDIO to "마이크",
        Manifest.permission.MODIFY_AUDIO_SETTINGS to "오디오 설정",
        Manifest.permission.ACCESS_FINE_LOCATION to "정확한 위치",
        Manifest.permission.ACCESS_COARSE_LOCATION to "대략적 위치",
        Manifest.permission.CALL_PHONE to "전화 걸기"
    )

    // 권한 상태 업데이트 함수
    fun updatePermissionStates() {
        permissionStates = permissions.associate { (permission, _) ->
            permission to (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    // 초기 권한 상태 확인
    LaunchedEffect(Unit) {
        updatePermissionStates()
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        updatePermissionStates()
        results.forEach { (permission, granted) ->
            println("권한 결과: $permission = $granted")
        }
    }

    // 단일 권한 요청 런처
    val singlePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        updatePermissionStates()
        println("마이크 권한 결과: $granted")
    }

    // 설정 화면으로 이동
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🔍 권한 상태 확인",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 권한 상태 표시
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(permissions.size) { index ->
                val (permission, name) = permissions[index]
                val isGranted = permissionStates[permission] ?: false

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGranted) {
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        } else {
                            Color(0xFFF44336).copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = permission.substringAfterLast("."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGranted) "허용됨" else "거부됨",
                                fontWeight = FontWeight.Bold,
                                color = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 권한 요청 버튼들
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 마이크 권한만 요청
            Button(
                onClick = {
                    println("🎤 마이크 권한 요청 중...")
                    singlePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("마이크 권한 요청")
            }

            // 모든 권한 요청
            Button(
                onClick = {
                    println("📋 모든 권한 요청 중...")
                    val deniedPermissions = permissions.mapNotNull { (permission, _) ->
                        if (permissionStates[permission] != true) permission else null
                    }.toTypedArray()

                    if (deniedPermissions.isNotEmpty()) {
                        permissionLauncher.launch(deniedPermissions)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("모든 권한 요청")
            }

            // 앱 설정으로 이동
            OutlinedButton(
                onClick = {
                    println("⚙️ 앱 설정 화면으로 이동")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    settingsLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("앱 설정에서 권한 허용")
            }

            // 권한 상태 새로고침
            OutlinedButton(
                onClick = {
                    println("🔄 권한 상태 새로고침")
                    updatePermissionStates()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("상태 새로고침")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 현재 상태 요약
        val totalPermissions = permissions.size
        val grantedPermissions = permissionStates.values.count { it }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (grantedPermissions == totalPermissions) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    Color(0xFFFF9800).copy(alpha = 0.1f)
                }
            )
        ) {
            Text(
                text = "📊 권한 상태: $grantedPermissions/$totalPermissions 허용됨",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}