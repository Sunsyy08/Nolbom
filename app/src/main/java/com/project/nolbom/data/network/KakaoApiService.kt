package com.project.nolbom.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class KakaoAddressResponse(
    val documents: List<Document>
) {
    data class Document(val address: Address)
    data class Address(val x: String, val y: String)
}

interface KakaoApiService {
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") auth: String,
        @Query("query") query: String
    ): KakaoAddressResponse
}
