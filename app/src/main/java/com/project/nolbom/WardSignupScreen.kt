package com.project.nolbom

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.project.nolbom.data.model.WardSignupRequest
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.launch
import com.project.nolbom.data.network.NetworkModule
import com.project.nolbom.data.network.KakaoAddressResponse  // 추가


@Composable
fun WardSignupScreen(
    userId: Long,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val profilePlaceholder = painterResource(id = R.drawable.ward_profile)
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var medicalStatus by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var latLng      by remember { mutableStateOf<Pair<String, String>?>(null) }
    var safeRadius  by remember { mutableStateOf("") }

    var isLoading       by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    // 카메라 론처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) profileBitmap = bitmap
    }
    val focusManager = LocalFocusManager.current
    val weightRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
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
                // 프로필 이미지와 카메라 버튼
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 원형 프로필 이미지
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { cameraLauncher.launch(null) }
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap!!.asImageBitmap(),
                                contentDescription = "프로필 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = profilePlaceholder,
                                contentDescription = "프로필 기본 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // 카메라 버튼: 프로필 원 위에 겹쳐 표시
                    IconButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "카메라",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "환영합니다!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "노약자 전용 회원가입",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        // 하단 폼 섹션
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
                // 기존의 집 주소 입력 필드 바로 아래에 붙여주세요
                OutlinedTextField(
                    value = homeAddress,
                    onValueChange = { homeAddress = it },
                    label = { Text("집 주소") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )

                Button(onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            // RetrofitClient.api 대신 NetworkModule.kakaoApi 로 호출
                            val resp = NetworkModule.kakaoApi.searchAddress(homeAddress)
                            latLng = resp.documents.firstOrNull()?.address?.let { it.y to it.x }

                            // resp.documents 에는 Document.address 가 있고, 그 안에 x,y 가 있습니다.
                            latLng = resp.documents
                                .firstOrNull()
                                ?.address
                                ?.let { it.y to it.x }

                        } catch(e: Exception) {
                            errorMessage = e.message ?: "주소 변환 실패"
                            showErrorDialog = true
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("주소 검색")
                }

                // 좌표가 세팅되면 화면에 보여줍니다
                latLng?.let { (lat, lng) ->
                    Text("위도: $lat", modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                    Text("경도: $lng", modifier = Modifier.padding(start = 8.dp))
                }


                OutlinedTextField(
                    value = medicalStatus,
                    onValueChange = { medicalStatus = it },
                    label = { Text("현재 의학 상태") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("키 (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (!height.endsWith(" cm")) height += " cm"
                                weightRequester.requestFocus()
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("몸무게 (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!weight.endsWith(" kg")) weight += " kg"
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .focusRequester(weightRequester)
                    )
                }

                // 기존 키/몸무게 입력 Row 다음, Spacer 전에 추가하세요
                OutlinedTextField(
                    value = safeRadius,
                    onValueChange = { safeRadius = it },
                    label = { Text("안전 반경 (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )


                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // 빈 값 체크
                        when {
                            height.isBlank() -> errorMessage = "키를 입력해주세요"
                            weight.isBlank() -> errorMessage = "몸무게를 입력해주세요"
                            medicalStatus.isBlank() -> errorMessage = "의학 상태를 입력해주세요"
                            homeAddress.isBlank() -> errorMessage = "집 주소를 입력해주세요"
                            else -> {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val rawHeight = height
                                            .replace("[^\\d.]".toRegex(), "")  // 숫자와 소수점만 남김
                                        val rawWeight = weight
                                            .replace("[^\\d.]".toRegex(), "")

                                        val heightVal = rawHeight.toFloat()
                                        val weightVal = rawWeight.toFloat()
                                        val req = WardSignupRequest(
                                            height        = heightVal,
                                            weight        = weightVal,
                                            medicalStatus = medicalStatus,
                                            homeAddress   = homeAddress,
                                            safeLat       = latLng?.first?.toDouble() ?: 0.0,
                                            safeLng       = latLng?.second?.toDouble() ?: 0.0,
                                            safeRadius    = safeRadius.toIntOrNull() ?: 0
                                        )
                                        val resp = RetrofitClient.api.signupWard(userId, req)
                                        if (!resp.success) throw Exception(resp.message)

                                        // 저장 후 메인으로 이동
                                        navController.navigate(Screen.Main.route) {
                                            popUpTo(Screen.WardSignup.route) { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.localizedMessage ?: "노약자 정보 저장 실패"
                                        showErrorDialog = true
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("시작하기")
                }
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title   = { Text("오류") },
                    text    = { Text(errorMessage) },
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
