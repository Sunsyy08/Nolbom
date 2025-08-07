package com.project.nolbom.data.network

import com.project.nolbom.data.model.CaptureResponse
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


// CaptureApiService.kt
interface CaptureApiService {
    @Multipart
    @POST("capture")
    suspend fun uploadCapture(
        @Part file: MultipartBody.Part
    ): CaptureResponse
}

object CaptureRetrofit {
    val api: CaptureApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost:5500/")    // FastAPI 서버
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CaptureApiService::class.java)
    }
}
