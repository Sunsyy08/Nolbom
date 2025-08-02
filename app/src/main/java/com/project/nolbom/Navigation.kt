package com.project.nolbom

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NolbomNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Start.route) {

        composable(Screen.Start.route) {
            StartScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController) // ← 필요 시 파라미터 맞춰주세요
        }
        composable(Screen.SignUpExtra.route) {
            SignUpExtraScreen(navController = navController)
        }
        composable(Screen.WardSignup.route){
            WardSignupScreen(navController = navController)
        }

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
