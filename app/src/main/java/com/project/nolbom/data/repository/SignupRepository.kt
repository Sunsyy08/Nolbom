// data/repository/SignupRepository.kt
package com.project.nolbom.data.repository

import com.project.nolbom.data.model.GenericResponse
import com.project.nolbom.data.model.WardSignupRequest
import com.project.nolbom.data.network.ApiService
import com.project.nolbom.data.network.NetworkModule
import com.project.nolbom.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignupRepository(
    private val api: ApiService = RetrofitClient.api // or NetworkModule.apiService
) {
    suspend fun signupWard(
        userId: Long,
        request: WardSignupRequest
    ): GenericResponse = withContext(Dispatchers.IO) {
        api.signupWard(userId, request)
    }
}
