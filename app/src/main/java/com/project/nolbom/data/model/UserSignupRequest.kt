package com.project.nolbom.data.model


/**
 * 회원가입 요청 바디로 보낼 데이터 클래스
 */
data class UserSignupRequest(
    val name: String,
    val email: String,
    val password: String
)
