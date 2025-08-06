package com.project.nolbom.data.network

import com.project.nolbom.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    // 1) 헤더 인터셉터 정의
    private val authInterceptor = Interceptor { chain ->
        val newReq = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
            .build()
        chain.proceed(newReq)
    }

    // 2) OkHttpClient에 인터셉터 등록
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // 3) Moshi에 Kotlin 어댑터 추가
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 4) Retrofit에 client, baseUrl, ConverterFactory 세팅
    private val retrofit = Retrofit.Builder()
        .client(httpClient)                                      // ← 여기에 적용
        .baseUrl("https://dapi.kakao.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // 5) API 서비스 객체 생성
    val kakaoApi: KakaoApiService = retrofit.create(KakaoApiService::class.java)
}
