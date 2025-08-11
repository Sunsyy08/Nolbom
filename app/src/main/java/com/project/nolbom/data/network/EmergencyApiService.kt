package com.project.nolbom.data.network

import com.project.nolbom.data.model.ApiResponse
import com.project.nolbom.data.model.DashboardStats
import com.project.nolbom.data.model.EmergencyReport
import com.project.nolbom.data.model.EmergencyReportRequest
import com.project.nolbom.data.model.EmergencyReportResponse
import com.project.nolbom.data.model.ReportsResponse
import com.project.nolbom.data.model.WardStatsResponse
import com.project.nolbom.data.model.WardsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.*

interface EmergencyApiService {

    // 응급 신고 저장
    @POST("api/emergency/report")
    suspend fun saveEmergencyReport(
        @Body request: EmergencyReportRequest
    ): Response<EmergencyReportResponse>

    // 응급 신고 목록 조회
    @GET("api/emergency/reports")
    suspend fun getEmergencyReports(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("user_name") userName: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("ward_id") wardId: Int? = null
    ): Response<ReportsResponse>

    // 대시보드 통계
    @GET("api/emergency/stats/dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    // 노약자별 통계
    @GET("api/emergency/stats/wards")
    suspend fun getWardStats(
        @Query("period") period: Int = 30
    ): Response<WardStatsResponse>

    // 노약자 목록
    @GET("api/emergency/wards")
    suspend fun getWards(): Response<WardsResponse>

    // 특정 노약자의 신고 이력
    @GET("api/emergency/wards/{ward_id}/reports")
    suspend fun getWardReports(
        @Path("ward_id") wardId: Int,
        @Query("limit") limit: Int = 10
    ): Response<ReportsResponse>

    // 최근 신고 (간단 조회)
    @GET("api/emergency/recent")
    suspend fun getRecentReports(): Response<ApiResponse<List<EmergencyReport>>>

    // 응급 시스템 상태
    @GET("api/emergency/status")
    suspend fun getEmergencyStatus(): Response<ApiResponse<Any>>

    // 신고 기록 삭제 (관리용)
    @DELETE("api/emergency/reports/{id}")
    suspend fun deleteEmergencyReport(
        @Path("id") reportId: Int
    ): Response<ApiResponse<String>>
}