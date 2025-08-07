package com.project.nolbom.utils  // 실제 패키지명에 맞춰 고치세요

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * multipart/form-data 로 보낼 때
 * text/plain RequestBody 로 변환해주는 헬퍼
 */
fun String.toPlainPart(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())
