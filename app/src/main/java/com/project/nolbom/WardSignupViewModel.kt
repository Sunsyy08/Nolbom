package com.project.nolbom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.nolbom.data.model.WardSignupRequest
import com.project.nolbom.data.repository.AddressRepository
import com.project.nolbom.data.repository.SignupRepository    // ← 추가!
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WardSignupViewModel(
    private val repo: AddressRepository = AddressRepository(),
    private val signupRepo: SignupRepository = SignupRepository() // ← 정의
) : ViewModel() {

    var homeAddress by mutableStateOf("")       // 입력한 주소
        private set

    var latLng by mutableStateOf<Pair<String, String>?>(null)  // (위도, 경도)
        private set

    var safeRadius by mutableStateOf("")         // 입력한 안전 반경(m)
        private set

    // **추가**: 키, 몸무게, 의학 상태 필드
    var height by mutableStateOf("")            // 키(cm)
        private set
    var weight by mutableStateOf("")            // 몸무게(kg)
        private set
    var medicalStatus by mutableStateOf("")     // 의학 상태
        private set

    var loading by mutableStateOf(false)         // 로딩 상태
        private set

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
            launch(Dispatchers.Main) {
                latLng = result
                loading = false
            }
        }
    }

    // 실제 서버에 가입 요청 보내는 함수
    fun submitSignup(userId: Long) {
        val coords = latLng ?: return
        val radius = safeRadius.toIntOrNull() ?: return
        val h = height.toFloatOrNull() ?: return
        val w = weight.toFloatOrNull() ?: return
        val ms = medicalStatus.ifBlank { return }

        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            val req = WardSignupRequest(
                height         = h,
                weight         = w,
                medicalStatus  = ms,
                homeAddress    = homeAddress,
                safeLat        = coords.first.toDouble(),
                safeLng        = coords.second.toDouble(),
                safeRadius     = radius
            )
            val resp = signupRepo.signupWard(userId, req)
            // TODO: 성공/실패 처리 (예: 토스트, 내비게이션)
            loading = false
        }
    }
}
