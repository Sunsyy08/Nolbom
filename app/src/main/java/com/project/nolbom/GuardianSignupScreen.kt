package com.project.nolbom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun GuardianSignupScreen(
    navController: NavController
) {
    val homeAddress = remember { mutableStateOf("") }
    val relation     = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(16.dp),
            modifier             = Modifier.align(Alignment.TopCenter)
        ) {
            // 집 주소 입력
            OutlinedTextField(
                value       = homeAddress.value,
                onValueChange = { homeAddress.value = it },
                label       = { Text("집 주소") },
                modifier    = Modifier.fillMaxWidth()
            )

            // 노약자와의 관계 입력
            OutlinedTextField(
                value         = relation.value,
                onValueChange = { relation.value = it },
                label         = { Text("노약자와의 관계") },
                modifier      = Modifier.fillMaxWidth()
            )
        }

        // 바로 시작하기 버튼
        Button(
            onClick = {
                // TODO: 가입 완료 후 네비게이션 처리
                // e.g. navController.navigate(Screen.MainHome.route)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("바로 시작하기", color = Color.White, fontSize = 16.sp)
        }
    }
}
