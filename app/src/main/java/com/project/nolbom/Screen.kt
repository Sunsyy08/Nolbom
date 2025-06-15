package com.project.nolbom

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AlertList : Screen("alert_list")
}