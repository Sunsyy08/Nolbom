package com.project.nolbom

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Main : Screen("main")
    object AlertList : Screen("alert_list")
    object SignUp : Screen("signup")
    object SignUpExtra   : Screen("signup/extra/{userId}") {
        // 라우팅 시 사용할 함수
        fun createRoute(userId: Long) = "signup/extra/$userId"
    }
    object WardSignup        : Screen("signup/ward/{userId}") {
        fun createRoute(userId: Long) = "signup/ward/$userId"
    }
    object GuardianSignup    : Screen("signup/guardian/{userId}") {
        fun createRoute(userId: Long) = "signup/guardian/$userId"
    }
    object Profile : Screen("profile")
    object FullMapp : Screen("fullmap")
}