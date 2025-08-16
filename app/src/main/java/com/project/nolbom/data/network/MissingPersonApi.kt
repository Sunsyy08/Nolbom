// MissingPersonsApi.kt
package com.project.nolbom.data.network

import com.project.nolbom.data.model.*
import retrofit2.http.*
import com.project.nolbom.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MissingPersonsApi {

    @GET("api/missing")
    suspend fun getMissingPersons(
        @Query("status") status: String = "MISSING",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<MissingPersonsResponse>

    @GET("api/missing/{id}")
    suspend fun getMissingPersonDetail(
        @Path("id") id: Int
    ): Response<MissingPersonResponse>

    @PUT("api/missing/{id}/found")
    suspend fun markAsFound(
        @Path("id") id: Int,
        @Body request: FoundRequest
    ): Response<MissingPersonResponse>

    @PUT("api/missing/{id}/location")
    suspend fun updateLocation(
        @Path("id") id: Int,
        @Body request: LocationUpdateRequest
    ): Response<MissingPersonResponse>

    // 헬스체크 - 🔧 기존 ApiResponse 사용
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
}