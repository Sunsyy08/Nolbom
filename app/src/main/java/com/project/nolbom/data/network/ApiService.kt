package com.project.nolbom.data.network

import com.project.nolbom.data.model.*
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// 로그인 요청/응답 모델
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user_id: Long?,
    val role: String?,
    val name: String?,
    val error: String?
)

interface ApiService {
    @POST("signup")
    suspend fun signup(
        @Body req: UserSignupRequest
    ): SignupResponse

    @POST("extra/{user_id}")
    suspend fun signupExtra(
        @Path("user_id") userId: Long,
        @Body req: SignupExtraRequest
    ): GenericResponse

    // 🔧 노약자 회원가입 반환 타입 변경
    @Multipart
    @POST("signup/ward/{userId}")
    suspend fun signupWardMultipart(
        @Path("userId") userId: Long,
        @Part("height") height: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("medical_status") medicalStatus: RequestBody,
        @Part("home_address") homeAddress: RequestBody,
        @Part("safe_lat") safeLat: RequestBody,
        @Part("safe_lng") safeLng: RequestBody,
        @Part("safe_radius") safeRadius: RequestBody,
        @Part profileImageFile: MultipartBody.Part
    ): WardSignupResponse    // 🔧 GenericResponse → WardSignupResponse

    @POST("signup/guardian/{user_id}")
    suspend fun signupGuardian(
        @Path("user_id") userId: Long,
        @Body req: GuardianSignupRequest
    ): GuardianResponse      // 🔧 GenericResponse → GuardianResponse

    @Multipart
    @POST("capture")
    suspend fun uploadCapture(
        @Part file: MultipartBody.Part
    ): CaptureResponse

    @POST("login")
    suspend fun login(
        @Body req: LoginRequest
    ): Response<LoginResponse>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserProfile>>

    @GET("user/profile-image")
    suspend fun getProfileImage(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @GET("/user/full-profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
}