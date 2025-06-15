package com.project.nolbom

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NolbomNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAlertList = {
                    navController.navigate(Screen.AlertList.route)
                }
            )
        }
        composable(Screen.AlertList.route) {
            AlertListScreen()
        }
    }
}
