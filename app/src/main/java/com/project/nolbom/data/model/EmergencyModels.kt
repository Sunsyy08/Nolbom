package com.project.nolbom.data.model

// EmergencyModels.kt - 응급 신고 관련 데이터 클래스
import com.google.gson.annotations.SerializedName

// 응급 신고 요청
data class EmergencyReportRequest(
    @SerializedName("user_name") val userName: String,
    @SerializedName("detected_keyword") val detectedKeyword: String,
    @SerializedName("ward_id") val wardId: Int? = null
)

// 응급 신고 응답
data class EmergencyReportResponse(
    val success: Boolean,
    @SerializedName("report_id") val reportId: Int? = null,
    @SerializedName("ward_id") val wardId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

// 응급 신고 기록
data class EmergencyReport(
    val id: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("detected_keyword") val detectedKeyword: String,
    @SerializedName("ward_id") val wardId: Int? = null,
    @SerializedName("report_time") val reportTime: String,

    // 노약자 정보 (JOIN된 데이터)
    @SerializedName("home_address") val homeAddress: String? = null,
    @SerializedName("medical_status") val medicalStatus: String? = null,
    @SerializedName("emergency_contact_1") val emergencyContact1: String? = null,
    @SerializedName("emergency_contact_2") val emergencyContact2: String? = null,
    @SerializedName("ward_name") val wardName: String? = null,
    @SerializedName("ward_phone") val wardPhone: String? = null
)

// 노약자 정보
data class Ward(
    @SerializedName("ward_id") val wardId: Int,
    @SerializedName("ward_name") val wardName: String? = null,
    @SerializedName("ward_phone") val wardPhone: String? = null,
    @SerializedName("home_address") val homeAddress: String? = null,
    @SerializedName("medical_status") val medicalStatus: String? = null,
    @SerializedName("emergency_contact_1") val emergencyContact1: String? = null,
    @SerializedName("total_reports") val totalReports: Int = 0,
    @SerializedName("last_report") val lastReport: String? = null
)

// 대시보드 통계
data class DashboardStats(
    val success: Boolean,
    val dashboard: DashboardData? = null,
    val error: String? = null
)

data class DashboardData(
    @SerializedName("today_reports") val todayReports: Int,
    @SerializedName("total_reports") val totalReports: Int,
    @SerializedName("recent_reports") val recentReports: List<EmergencyReport>
)

// 노약자별 통계
data class WardStatsResponse(
    val success: Boolean,
    @SerializedName("period_days") val periodDays: Int,
    @SerializedName("ward_stats") val wardStats: List<WardStat>? = null,
    val error: String? = null
)

data class WardStat(
    @SerializedName("ward_id") val wardId: Int,
    @SerializedName("ward_name") val wardName: String? = null,
    @SerializedName("ward_phone") val wardPhone: String? = null,
    @SerializedName("home_address") val homeAddress: String? = null,
    @SerializedName("report_count") val reportCount: Int,
    @SerializedName("last_report") val lastReport: String? = null,
    @SerializedName("first_report") val firstReport: String? = null
)

// 응답 공통 형태
data class ApiResponses<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

// 페이지네이션
data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// 신고 목록 응답
data class ReportsResponse(
    val success: Boolean,
    val reports: List<EmergencyReport>? = null,
    val pagination: Pagination? = null,
    val error: String? = null
)

// 노약자 목록 응답
data class WardsResponse(
    val success: Boolean,
    val wards: List<Ward>? = null,
    val error: String? = null
)
