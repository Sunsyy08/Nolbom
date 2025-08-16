package com.project.nolbom.list
// 🆕 백엔드 실종자 데이터를 AlertUser로 변환하는 확장 함수
import com.project.nolbom.data.model.MissingPerson


data class AlertUser(
    val name: String,
    val age: String,
    val height: String,
    val weight: String,
    val location: String
)
fun MissingPerson.toAlertUser(): AlertUser {
    return AlertUser(
        name = this.name,
        age = "${this.age}세",
        height = "${this.height}cm",
        weight = "${this.weight}kg",
        location = this.current_lat?.let { lat ->
            this.current_lng?.let { lng ->
                "위도: ${"%.4f".format(lat)}, 경도: ${"%.4f".format(lng)}"
            }
        } ?: this.home_address ?: "위치 정보 없음"
    )
}

// 🆕 백엔드 실종자 목록을 AlertUser 목록으로 변환
fun List<MissingPerson>.toAlertUserList(): List<AlertUser> {
    return this.map { it.toAlertUser() }
}