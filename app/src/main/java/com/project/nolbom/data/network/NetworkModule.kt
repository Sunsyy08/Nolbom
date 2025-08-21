/**
 * 파일명: NetworkModule.kt
 * 위치: data/network/
 *
 * 설명:
 *  - Retrofit 기반 네트워크 모듈 정의 객체 (object)
 *  - Kakao REST API 호출을 위한 공통 설정을 관리
 *
 * 주요 구성 요소:
 *  1) authInterceptor
 *     - 모든 요청에 "Authorization" 헤더 추가
 *     - 헤더 값: KakaoAK + BuildConfig.KAKAO_REST_API_KEY
 *
 *  2) OkHttpClient
 *     - authInterceptor 등록하여 인증 헤더 자동 부착
 *
 *  3) Moshi
 *     - KotlinJsonAdapterFactory 적용하여 JSON 직렬화/역직렬화 지원
 *
 *  4) Retrofit
 *     - Base URL: https://dapi.kakao.com/
 *     - MoshiConverterFactory 적용
 *     - OkHttpClient 연결
 *
 *  5) API 서비스 객체
 *     - KakaoApiService 구현체 생성 및 외부에서 사용 가능하도록 제공
 *
 * 주의:
 *  - BuildConfig.KAKAO_REST_API_KEY는 local.properties → gradle을 통해 주입된 값 사용
 *  - Authorization 헤더 자동 추가되므로 별도 설정 불필요
 */

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
