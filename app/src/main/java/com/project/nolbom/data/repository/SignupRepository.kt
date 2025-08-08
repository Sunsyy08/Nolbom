// data/repository/SignupRepository.kt
package com.project.nolbom.data.repository

import android.content.Context
import com.project.nolbom.data.model.*
import com.project.nolbom.data.network.ApiService
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType  // 🔧 새로운 import 추가

class SignupRepository(
    private val api: ApiService = RetrofitClient.api,
    private val context: Context? = null
) {

    // UserRepository 인스턴스 (토큰 저장용)
    private val userRepository by lazy {
        context?.let { UserRepository(it) }
    }

    /**
     * 🔧 반환 타입을 WardSignupResponse로 변경
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
    ): WardSignupResponse = withContext(Dispatchers.IO) {
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

    /**
     * 🆕 노약자 회원가입 완료 및 토큰 저장 처리
     */
    suspend fun completeWardSignup(
        userId: Long,
        height: String,
        weight: String,
        medicalStatus: String,
        homeAddress: String,
        safeLat: String,
        safeLng: String,
        safeRadius: String,
        profileImageFile: MultipartBody.Part,
        userEmail: String,  // 회원가입 시 입력한 이메일
        userName: String    // 회원가입 시 입력한 이름
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. API 호출 - 🔧 MediaType 사용법 수정
            val response = signupWardMultipart(
                userId = userId,
                height = RequestBody.create("text/plain".toMediaType(), height),
                weight = RequestBody.create("text/plain".toMediaType(), weight),
                medicalStatus = RequestBody.create("text/plain".toMediaType(), medicalStatus),
                homeAddress = RequestBody.create("text/plain".toMediaType(), homeAddress),
                safeLat = RequestBody.create("text/plain".toMediaType(), safeLat),
                safeLng = RequestBody.create("text/plain".toMediaType(), safeLng),
                safeRadius = RequestBody.create("text/plain".toMediaType(), safeRadius),
                profileImageFile = profileImageFile
            )

            // 2. 응답 확인 및 데이터 저장
            if (response.success && response.token != null) {
                // 🎯 받은 데이터를 UserRepository에 저장
                userRepository?.saveTokenAndUserInfo(
                    token = response.token,
                    name = response.name ?: userName,
                    email = userEmail,
                    homeAddress = response.home_address ?: homeAddress,
                    profileImage = response.profile_image,
                    role = "ward"
                )

                Result.success("노약자 회원가입 완료")
            } else {
                Result.failure(Exception(response.error ?: "회원가입 실패"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 보호자 회원가입 완료 및 토큰 저장 처리
     */
    suspend fun completeGuardianSignup(
        userId: Long,
        wardEmail: String,
        address: String,
        relation: String,
        userEmail: String,  // 회원가입 시 입력한 이메일
        userName: String    // 회원가입 시 입력한 이름
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = GuardianSignupRequest(
                wardEmail = wardEmail,
                address = address,
                relation = relation
            )

            val response = api.signupGuardian(userId, request)

            if (response.success && response.token != null) {
                // 🎯 보호자 정보 저장
                userRepository?.saveTokenAndUserInfo(
                    token = response.token,
                    name = userName,
                    email = userEmail,
                    homeAddress = response.address ?: address,
                    profileImage = null, // 보호자는 프로필 이미지 없음
                    role = "guardian"
                )

                Result.success("보호자 회원가입 완료")
            } else {
                Result.failure(Exception(response.error ?: "회원가입 실패"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 기본 회원가입 (1단계)
     */
    suspend fun signup(
        name: String,
        email: String,
        password: String
    ): Result<SignupResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UserSignupRequest(
                name = name,
                email = email,
                password = password
            )

            val response = api.signup(request)

            if (response.success) {
                // 🎯 기본 회원가입 완료 시에도 토큰 저장 (Node.js에서 토큰 반환하는 경우)
                if (response.token != null) {
                    userRepository?.saveTokenAndUserInfo(
                        token = response.token,
                        name = response.name ?: name,
                        email = response.email ?: email,
                        homeAddress = null,
                        profileImage = null,
                        role = "user"
                    )
                }
                Result.success(response)
            } else {
                Result.failure(Exception(response.error ?: "회원가입 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 추가 정보 저장 (2단계)
     */
    suspend fun signupExtra(
        userId: Long,
        birthdate: String,
        phone: String,
        gender: String,
        role: String
    ): Result<GenericResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignupExtraRequest(
                birthdate = birthdate,
                phone = phone,
                gender = gender,
                role = role
            )

            val response = api.signupExtra(userId, request)

            if (response.success && response.token != null) {
                // 🎯 role 업데이트된 토큰 저장
                userRepository?.saveTokenAndUserInfo(
                    token = response.token,
                    name = userRepository?.getStoredUserName() ?: "사용자",
                    email = userRepository?.getStoredUserEmail() ?: "",
                    homeAddress = null,
                    profileImage = null,
                    role = response.role ?: role
                )
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 헬퍼 함수: 이미지 파일을 MultipartBody.Part로 변환
     */
    companion object {
        fun createImagePart(imageByteArray: ByteArray, filename: String = "profile.jpg"): MultipartBody.Part {
            val requestFile = RequestBody.create("image/jpeg".toMediaType(), imageByteArray)
            return MultipartBody.Part.createFormData("profile_image_file", filename, requestFile)
        }

        fun createTextPart(value: String): RequestBody {
            return RequestBody.create("text/plain".toMediaType(), value)
        }
    }
}