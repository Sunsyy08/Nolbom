/**
 * 파일명: RetrofitClient.kt
 * 위치: data/network/
 *
 * 설명:
 *  - Retrofit 기반 네트워크 클라이언트 모듈
 *  - Node.js 서버, Python STT 서버, WebSocket 연결 등을 위한 URL/클라이언트 제공
 *  - 프로젝트 내 모든 API 호출에 공통적으로 사용
 *
 * 주요 기능:
 *  1) BASE_URL, STT_BASE_URL 등 서버 IP/포트 관리
 *  2) getBaseUrl(), getImageUrl(), getWebSocketUrl() 등 URL 제공 함수
 *  3) OkHttpClient + HttpLoggingInterceptor 설정
 *  4) MoshiConverterFactory를 적용한 Retrofit 객체 생성
 *  5) ApiService, STTApiService, MissingPersonsApi 객체 제공
 *  6) 실종자 API 연결 테스트 함수 (testMissingPersonsConnection)
 *
 * 주의:
 *  - PC_IP는 주기적으로 변경될 수 있으므로 실제 사용 환경에 맞춰 수정 필요
 *  - TokenStore에서 JWT 토큰 제공 가능 (필요 시 Interceptor 추가 가능)
 *  - Retrofit 객체는 Lazy 또는 싱글톤 패턴으로 재사용 권장
 */
package com.project.nolbom.data.network

import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // 🔧 실제 네트워크 IP 사용
    // 몇 주기마다 PC_IP 수정 해야함 IP 주소 달라짐
    private const val PC_IP = "192.168.75.60"

    private const val BASE_URL = "http://$PC_IP:3000/"
    private const val STT_BASE_URL = "http://$PC_IP:8000/"

    // 🆕 IP 주소 공유 함수 추가
    fun getBaseUrl(): String = BASE_URL
    fun getImageUrl(endpoint: String): String = "$BASE_URL$endpoint"

    // 🆕 WebSocket용 URL 제공 함수 추가
    fun getWebSocketUrl(): String = "http://$PC_IP:3000"

    init {
        println("🔍 네트워크 설정:")
        println("🔍 PC IP: $PC_IP")
        println("🔍 Node.js: $BASE_URL")
        println("🔍 Python STT: $STT_BASE_URL")
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val tokenProvider: () -> String? = {
        TokenStore.getToken()
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ApiService::class.java)

    val sttApi: STTApiService = Retrofit.Builder()
        .baseUrl(STT_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(STTApiService::class.java)

    // 🔧 실종자 API 수정 - 올바른 baseUrl 사용
    val missingPersonsApi: MissingPersonsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // ← 기본 BASE_URL 사용 (http://10.183.172.236:3000/)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MissingPersonsApi::class.java)
    }

    // 🆕 연결 테스트 함수
    suspend fun testMissingPersonsConnection(): Boolean {
        return try {
            val response = missingPersonsApi.healthCheck()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}