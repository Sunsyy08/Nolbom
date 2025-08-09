// data/network/RetrofitClient.kt
package com.project.nolbom.data.network

import com.project.nolbom.data.local.TokenStore
import com.project.nolbom.data.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.project.nolbom.data.network.RetrofitClient


object RetrofitClient {
    private const val BASE_URL = "http://127.0.0.1:3000/"
  //  http://localhost:3000/

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // 토큰 저장소에서 토큰 꺼내오기 (SharedPreferences나 DataStore에서 구현)
    private val tokenProvider: () -> String? = {
        TokenStore.getToken() // 여기를 네가 쓰는 저장소 코드로 교체
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
}
