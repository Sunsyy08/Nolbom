// data/repository/SignupRepository.kt
package com.project.nolbom.data.repository

import com.project.nolbom.data.model.GenericResponse
import com.project.nolbom.data.network.ApiService
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SignupRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /**
     * multipart/form-data 로 가입 요청 보내기
     */
    suspend fun signupWardMultipart(
        userId: Long,
        height: RequestBody,
        weight: RequestBody,
        medicalStatus: RequestBody,
        homeAddress: RequestBody,
        safeLat: RequestBody,
        safeLng: RequestBody,
        safeRadius: RequestBody,
        profileImageFile: MultipartBody.Part
    ): GenericResponse = withContext(Dispatchers.IO) {
        api.signupWardMultipart(
            userId           = userId,
            height           = height,
            weight           = weight,
            medicalStatus    = medicalStatus,
            homeAddress      = homeAddress,
            safeLat          = safeLat,
            safeLng          = safeLng,
            safeRadius       = safeRadius,
            profileImageFile = profileImageFile
        )
    }
}
