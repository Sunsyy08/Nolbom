// MissingPersonsApi.kt
package com.project.nolbom.data.network

import com.project.nolbom.data.model.*
import retrofit2.http.*
import com.project.nolbom.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MissingPersonsApi {

    // 실종자 목록 조회
    @GET("missing-persons")
    suspend fun getMissingPersons(
        @Query("status") status: String = "MISSING",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<MissingPersonsResponse>

    // 특정 실종자 상세 조회 - 🔧 기존 ApiResponse 사용
    @GET("missing-persons/{id}")
    suspend fun getMissingPersonDetail(
        @Path("id") id: Int
    ): Response<ApiResponse<MissingPerson>>

    // 실종자 발견 처리 - 🔧 기존 ApiResponse 사용
    @PUT("missing-persons/{id}/found")
    suspend fun markAsFound(
        @Path("id") id: Int,
        @Body request: FoundRequest
    ): Response<ApiResponse<String>>

    // 위치 업데이트 - 🔧 기존 ApiResponse 사용
    @PUT("missing-persons/{id}/location")
    suspend fun updateLocation(
        @Path("id") id: Int,
        @Body request: LocationUpdateRequest
    ): Response<ApiResponse<String>>

    // 헬스체크 - 🔧 기존 ApiResponse 사용
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
}