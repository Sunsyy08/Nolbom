package com.project.nolbom

import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.ui.draw.blur
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
import com.project.nolbom.data.network.KakaoAddressResponse
import com.project.nolbom.utils.toPlainPart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun WardSignupScreen(
    userId: Long,
    navController: NavHostController,
    userEmail: String = "",
    userName: String = ""
) {
    val scope = rememberCoroutineScope()
    val profilePlaceholder = painterResource(id = R.drawable.ward_profile)
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var medicalStatus by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var latLng      by remember { mutableStateOf<Pair<String, String>?>(null) }
    var safeRadius  by remember { mutableStateOf("") }

    var isLoading       by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    val apiService = RetrofitClient.api
    val context    = LocalContext.current

    val focusManager     = LocalFocusManager.current
    val weightRequester = remember { FocusRequester() }

    val signupRepository = remember {
        com.project.nolbom.data.repository.SignupRepository(context = context)
    }

    var profileFilename  by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult

        val file = File(context.cacheDir, "capture.png").apply { delete() }
        ByteArrayOutputStream().use { baos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            file.writeBytes(baos.toByteArray())
        }
        val part = MultipartBody.Part.createFormData(
            "file", file.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
        scope.launch {
            try {
                val resp = apiService.uploadCapture(part)
                if (resp.success) {
                    profileBitmap   = bitmap
                    profileFilename = resp.filename
                    Toast.makeText(context, "얼굴 인식 및 저장 완료", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, resp.message ?: "얼굴 인식 실패", Toast.LENGTH_LONG).show()
                }
            } catch(e: Exception) {
                Toast.makeText(context, "업로드 오류: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFAAF0D1), Color(0xFFB2EBF2))
                )
            )
    ) {
        // 떠다니는 원들 - 전체 화면용 배치
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Color(0x25FFFFFF),
                    CircleShape
                )
                .blur(25.dp)
                .align(Alignment.TopStart)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Color(0x30FFFFFF),
                    CircleShape
                )
                .blur(18.dp)
                .align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color(0x20FFFFFF),
                    CircleShape
                )
                .blur(22.dp)
                .align(Alignment.CenterStart)
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color(0x35FFFFFF),
                    CircleShape
                )
                .blur(16.dp)
                .align(Alignment.CenterEnd)
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    Color(0x28FFFFFF),
                    CircleShape
                )
                .blur(20.dp)
                .align(Alignment.BottomStart)
        )

        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Color(0x32FFFFFF),
                    CircleShape
                )
                .blur(17.dp)
                .align(Alignment.BottomEnd)
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    Color(0x40FFFFFF),
                    CircleShape
                )
                .blur(14.dp)
                .align(Alignment.Center)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 프로필 이미지와 카메라 버튼
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 원형 프로필 이미지
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { cameraLauncher.launch(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileBitmap != null) {
                                Image(
                                    bitmap = profileBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = profilePlaceholder,
                                    contentDescription = "기본 프로필",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                        // 카메라 버튼
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
                    // 집 주소 입력
                    OutlinedTextField(
                        value = homeAddress,
                        onValueChange = { homeAddress = it },
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
                            .height(60.dp)
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                try {
                                    val resp = NetworkModule.kakaoApi.searchAddress(homeAddress)
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD1A5)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("주소 검색", fontWeight = FontWeight.SemiBold)
                    }

                    // 좌표 표시
                    latLng?.let { (lat, lng) ->
                        Text("위도: $lat", modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                        Text("경도: $lng", modifier = Modifier.padding(start = 8.dp))
                    }

                    // 의학 상태 입력
                    OutlinedTextField(
                        value = medicalStatus,
                        onValueChange = { medicalStatus = it },
                        label = { Text("현재 의학 상태", color = Color(0xFF666666)) },
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
                            .height(60.dp)
                    )

                    // 키, 몸무게 입력
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("키 (cm)", color = Color(0xFF666666)) },
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF4FD1A5),
                                focusedLabelColor = Color(0xFF4FD1A5),
                                cursorColor = Color(0xFF4FD1A5)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("몸무게 (kg)", color = Color(0xFF666666)) },
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF4FD1A5),
                                focusedLabelColor = Color(0xFF4FD1A5),
                                cursorColor = Color(0xFF4FD1A5)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .focusRequester(weightRequester)
                        )
                    }

                    // 안전 반경 입력
                    OutlinedTextField(
                        value = safeRadius,
                        onValueChange = { safeRadius = it },
                        label = { Text("안전 반경 (m)", color = Color(0xFF666666)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            .height(60.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 시작하기 버튼
                    Button(
                        onClick = {
                            when {
                                height.isBlank()       -> errorMessage = "키를 입력해주세요"
                                weight.isBlank()       -> errorMessage = "몸무게를 입력해주세요"
                                medicalStatus.isBlank()-> errorMessage = "의학 상태를 입력해주세요"
                                homeAddress.isBlank()  -> errorMessage = "집 주소를 입력해주세요"
                                profileBitmap == null  -> {
                                    Toast.makeText(context, "프로필을 먼저 등록해주세요", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val rawHeight = height.replace("[^\\d.]".toRegex(), "")
                                            val rawWeight = weight.replace("[^\\d.]".toRegex(), "")

                                            val outputStream = ByteArrayOutputStream()
                                            profileBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                                            val imageByteArray = outputStream.toByteArray()

                                            val imagePart = com.project.nolbom.data.repository.SignupRepository
                                                .createImagePart(imageByteArray, "profile.jpg")

                                            val result = signupRepository.completeWardSignup(
                                                userId = userId,
                                                height = rawHeight,
                                                weight = rawWeight,
                                                medicalStatus = medicalStatus,
                                                homeAddress = homeAddress,
                                                safeLat = latLng?.first ?: "0.0",
                                                safeLng = latLng?.second ?: "0.0",
                                                safeRadius = safeRadius,
                                                profileImageFile = imagePart,
                                                userEmail = userEmail,
                                                userName = userName
                                            )

                                            result.onSuccess { successMessage ->
                                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()

                                                val userRepository = com.project.nolbom.data.repository.UserRepository(context)
                                                userRepository.logStoredUserData()

                                                navController.navigate(Screen.Main.route) {
                                                    popUpTo(Screen.WardSignup.route) { inclusive = true }
                                                }
                                            }.onFailure { exception ->
                                                errorMessage = exception.message ?: "회원가입 실패"
                                                showErrorDialog = true
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4FD1A5),
                            disabledContainerColor = Color(0xFF4FD1A5).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        ) else Text(
                            "시작하기",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (showErrorDialog) {
                    AlertDialog(
                        onDismissRequest = { showErrorDialog = false },
                        title   = { Text("오류") },
                        text    = { Text(errorMessage) },
                        confirmButton = {
                            TextButton(onClick = { showErrorDialog = false }) {
                                Text("확인", color = Color(0xFF4FD1A5))
                            }
                        }
                    )
                }
            }
        }
    }
}