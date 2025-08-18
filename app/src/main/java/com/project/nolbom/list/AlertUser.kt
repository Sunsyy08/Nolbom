package com.project.nolbom.list
// 🆕 백엔드 실종자 데이터를 AlertUser로 변환하는 확장 함수
import com.project.nolbom.data.model.MissingPerson


// ✅ profile_image로 필드명 통일
data class AlertUser(
    val name: String,
    val age: String,
    val height: String,
    val weight: String,
    val location: String,
    val gender: String?,
    val profile_image: String?  // ✅ profileImage → profile_image로 변경
)

fun MissingPerson.toAlertUser(): AlertUser {
    // 🔍 프로필 이미지 디버깅
    val profileImageLog = if (this.profile_image != null) {
        "프로필 이미지 있음: ${this.profile_image.take(50)}..."
    } else {
        "프로필 이미지 없음"
    }
    println("🖼️ ${this.name}: $profileImageLog")

    return AlertUser(
        name = this.name,
        age = "${this.age}세",
        height = "${this.height}cm",
        weight = "${this.weight}kg",
        location = this.current_lat?.let { lat ->
            this.current_lng?.let { lng ->
                "위도: ${"%.4f".format(lat)}, 경도: ${"%.4f".format(lng)}"
            }
        } ?: this.home_address ?: "위치 정보 없음",
        gender = this.gender,
        profile_image = this.profile_image  // ✅ 이제 필드명이 일치함
    )
}

fun List<MissingPerson>.toAlertUserList(): List<AlertUser> {
    return this.map { it.toAlertUser() }
}