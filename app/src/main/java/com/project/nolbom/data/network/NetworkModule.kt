package com.project.nolbom.data.network

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object NetworkModule {
    private val moshi = Moshi.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://dapi.kakao.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val kakaoApi: KakaoApiService = retrofit.create(KakaoApiService::class.java)
}