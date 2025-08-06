package com.project.nolbom.data.repository

import com.project.nolbom.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.project.nolbom.BuildConfig


class AddressRepository {
    private val api = NetworkModule.kakaoApi
    private val key = "KakaoAK " + BuildConfig.KAKAO_REST_API_KEY

    suspend fun getLatLng(address: String): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            val resp = api.searchAddress(key, address)
            resp.documents.firstOrNull()?.address?.let {
                it.y to it.x
            }
        }
}