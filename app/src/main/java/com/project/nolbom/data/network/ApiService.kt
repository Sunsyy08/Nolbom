package com.project.nolbom.data.network


import com.project.nolbom.data.model.CaptureResponse
import com.project.nolbom.data.model.SignupResponse
import com.project.nolbom.data.model.GenericResponse
import com.project.nolbom.data.model.GuardianResponse
import com.project.nolbom.data.model.GuardianSignupRequest
import com.project.nolbom.data.model.UserSignupRequest
import com.project.nolbom.data.model.SignupExtraRequest
import com.project.nolbom.data.model.WardSignupRequest
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    /**
     * 1단계: 기본 회원가입
     * POST http://<BASE_URL>/signup
     */
    @POST("signup")
    suspend fun signup(
        @Body req: UserSignupRequest
    ): SignupResponse

    /**
     * 2단계: 추가 정보 제출
     * POST http://<BASE_URL>/extra/{user_id}
     */
    @POST("extra/{user_id}")
    suspend fun signupExtra(
        @Path("user_id") userId: Long,
        @Body req: SignupExtraRequest
    ): GenericResponse
    /**
     * 3단계: 노약자 전용 회원가입
     * POST http://<BASE_URL>/signup/ward/{user_id}
     */
    @Multipart
    @POST("signup/ward/{userId}")
    suspend fun signupWardMultipart(
        @Path("userId") userId: Long,

        @Part("height")       height: RequestBody,
        @Part("weight")       weight: RequestBody,
        @Part("medical_status")  medicalStatus: RequestBody,
        @Part("home_address")    homeAddress: RequestBody,
        @Part("safe_lat")        safeLat: RequestBody,
        @Part("safe_lng")        safeLng: RequestBody,
        @Part("safe_radius")     safeRadius: RequestBody,

        // 파일은 별도 Part
        @Part profileImageFile: MultipartBody.Part
    ): GenericResponse
    // data/network/ApiService.kt
    @POST("signup/guardian/{user_id}")
    suspend fun signupGuardian(
        @Path("user_id") userId: Long,
        @Body req: GuardianSignupRequest
    ): GenericResponse

    @Multipart
    @POST("capture")
    suspend fun uploadCapture(
        @Part file: MultipartBody.Part
    ): CaptureResponse

}

