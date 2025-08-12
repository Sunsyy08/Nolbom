// data/network/RetrofitClient.kt - 기존 코드에 STT API만 추가
package com.project.nolbom.data.network

import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    // 기존 Node.js API
    private const val BASE_URL = "http://127.0.0.1:3000/"

    // 🆕 Python STT API URL 추가
    private const val STT_BASE_URL = "http://127.0.0.1:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 토큰 저장소에서 토큰 꺼내오기 (기존 코드 유지)
    private val tokenProvider: () -> String? = {
        TokenStore.getToken()
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 기존 Node.js API (기존 코드 그대로 유지)
    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ApiService::class.java)

    // 🆕 Python STT API만 추가
    val sttApi: STTApiService = Retrofit.Builder()
        .baseUrl(STT_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(STTApiService::class.java)
}