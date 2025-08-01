package com.project.nolbom

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Main : Screen("main")
    object AlertList : Screen("alert_list")
    object SignUp : Screen("signup")
}