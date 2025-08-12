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
    private const val PC_IP = "10.183.172.236"

    private const val BASE_URL = "http://$PC_IP:3000/"
    private const val STT_BASE_URL = "http://$PC_IP:8000/"

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
}