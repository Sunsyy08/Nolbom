/**
 * 파일명: PermissionHelper.kt
 * 위치: utils/
 *
 * 설명:
 *  - Jetpack Compose 환경에서 RECORD_AUDIO 권한 요청 및 확인
 *  - Composable을 통해 버튼 클릭 등 이벤트에 권한 요청 로직 연결 가능
 *  - 권한 승인/거부 콜백(onPermissionGranted, onPermissionDenied) 제공
 *
 * 주요 기능:
 *  1) RequestAudioPermission Composable:
 *      - 권한이 없는 경우 런처 실행
 *      - 승인/거부 콜백 처리
 *      - content 람다를 통해 UI 구성 가능
 *  2) hasAudioPermission(context: Context) 함수:
 *      - 현재 권한 상태 확인
 *
 * 주의:
 *  - AndroidManifest.xml에 RECORD_AUDIO 권한 선언 필요
 *  - Android 6.0 이상에서 런타임 권한 요청 필요
 */

package com.project.nolbom.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

@Composable
fun RequestAudioPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    val requestPermission = {
        launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    content(requestPermission)
}

// 권한 확인 함수
fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}