/**
 * 파일명: ApiService.kt
 * 위치: data/network/
 *
 * 설명:
 *  - Retrofit 인터페이스 정의 파일
 *  - 서버와의 HTTP 통신(회원가입, 로그인, 프로필 조회 등)을 관리
 *
 * 주요 엔드포인트:
 *  - POST /signup → 회원가입 (기본 정보)
 *  - POST /extra/{user_id} → 회원 추가정보 등록
 *  - POST /signup/ward/{userId} (Multipart) → 노약자 회원가입 (프로필 이미지 포함)
 *  - POST /signup/guardian/{user_id} → 보호자 회원가입
 *  - POST /capture (Multipart) → 캡처 이미지 업로드
 *  - POST /login → 로그인 (이메일/비밀번호)
 *  - GET /user/profile → 사용자 프로필 조회
 *  - GET /user/profile-image → 사용자 프로필 이미지 조회
 *  - GET /user/full-profile → 사용자 전체 프로필 조회
 *
 * 반환 타입:
 *  - Retrofit Response<> 객체 또는 커스텀 Response 모델
 *  - 일부 엔드포인트는 GenericResponse → 전용 Response 모델(WardSignupResponse, GuardianResponse 등)로 교체
 *
 * 주의:
 *  - @Header("Authorization") 필요 시 JWT 토큰을 포함해야 함
 *  - Multipart 요청은 RequestBody + MultipartBody.Part 조합으로 전송
 */

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
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    // ApiService: (A 방식 쓰는 중이면 @Header 유지)
    @GET("user/profile-image")
    suspend fun getProfileImage(@Header("Authorization") auth: String): Response<ResponseBody>


    @GET("user/full-profile")
    suspend fun getProfile(@Header("Authorization") auth: String): Response<ProfileResponse>
}