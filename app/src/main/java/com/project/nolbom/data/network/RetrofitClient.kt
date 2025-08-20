// data/network/RetrofitClient.kt - 올바른 IP 설정
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