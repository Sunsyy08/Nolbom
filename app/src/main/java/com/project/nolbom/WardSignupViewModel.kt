package com.project.nolbom

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.nolbom.data.repository.AddressRepository
import com.project.nolbom.data.repository.SignupRepository
import com.project.nolbom.utils.toPlainPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class WardSignupViewModel(
    private val repo: AddressRepository = AddressRepository(),
    private val signupRepo: SignupRepository = SignupRepository()
) : ViewModel() {

    var profileFilename by mutableStateOf<String?>(null)
        private set
    var profileBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var homeAddress by mutableStateOf("")
        private set
    var latLng by mutableStateOf<Pair<String, String>?>(null)
        private set
    var safeRadius by mutableStateOf("")
        private set

    var height by mutableStateOf("")
        private set
    var weight by mutableStateOf("")
    private set
    var medicalStatus by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    /**
     * 프로필 비트맵 상태 업데이트
     */
    fun updateProfileBitmap(bitmap: Bitmap) {
        profileBitmap = bitmap
    }

    // 주소 입력 핸들러
    fun onAddressChange(addr: String) {
        homeAddress = addr
    }

    // 반경 입력 핸들러
    fun onRadiusChange(radius: String) {
        safeRadius = radius
    }

    // 키 입력 핸들러
    fun onHeightChange(value: String) {
        height = value
    }

    // 몸무게 입력 핸들러
    fun onWeightChange(value: String) {
        weight = value
    }

    // 의학 상태 입력 핸들러
    fun onMedicalStatusChange(value: String) {
        medicalStatus = value
    }

    // 주소 → 위경도 조회
    fun lookupAddress() {
        if (homeAddress.isBlank()) return
        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.getLatLng(homeAddress)
            withContext(Dispatchers.Main) {
                latLng = result
                loading = false
            }
        }
    }

    /**
     * multipart/form-data 로 프로필 이미지와 데이터를 전송
     */
    fun submitSignupMultipart(context: Context, userId: Long) {
        val bmp = profileBitmap ?: return
        val coords = latLng ?: return
        val radius = safeRadius.toIntOrNull() ?: return
        val h = height.toFloatOrNull() ?: return
        val w = weight.toFloatOrNull() ?: return
        val ms = medicalStatus.ifBlank { return }
        val addr = homeAddress.ifBlank { return }

        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            // 1) Bitmap -> 파일
            val file = File(context.cacheDir, "profile.png").apply {
                if (exists()) delete()
                createNewFile()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream())
            }

            // 2) 이미지 파트
            val imageReq = file
                .asRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "profile_image_file", file.name, imageReq
            )

            // 3) 텍스트 파트
            val partHeight = h.toString().toPlainPart()
            val partWeight = w.toString().toPlainPart()
            val partMS     = ms.toPlainPart()
            val partAddr   = addr.toPlainPart()
            val partLat    = coords.first.toPlainPart()
            val partLng    = coords.second.toPlainPart()
            val partRadius = radius.toString().toPlainPart()

            // 4) Repository 호출
            val resp = signupRepo.signupWardMultipart(
                userId           = userId,
                height           = partHeight,
                weight           = partWeight,
                medicalStatus    = partMS,
                homeAddress      = partAddr,
                safeLat          = partLat,
                safeLng          = partLng,
                safeRadius       = partRadius,
                profileImageFile = imagePart
            )

            withContext(Dispatchers.Main) {
                loading = false
                // TODO: 성공/실패 처리 (예: Toast, 내비게이션)
            }
        }
    }
}

