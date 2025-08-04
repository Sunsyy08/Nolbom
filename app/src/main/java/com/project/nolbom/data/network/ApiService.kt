package com.project.nolbom.data.network


import com.project.nolbom.data.model.SignupResponse
import com.project.nolbom.data.model.GenericResponse
import com.project.nolbom.data.model.UserSignupRequest
import com.project.nolbom.data.model.SignupExtraRequest
import retrofit2.http.Body
import retrofit2.http.POST
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
}

