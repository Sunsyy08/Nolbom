package com.project.nolbom

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.nolbom.ui.signup.SignUpExtraScreen


@Composable
fun NolbomNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Start.route) {

        composable(Screen.Start.route) {
            StartScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController) // ← 필요 시 파라미터 맞춰주세요
        }
        composable(
            route = Screen.SignUpExtra.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            // backStackEntry.arguments 의 키도 "userId" 여야 꺼낼 수 있습니다.
            val userId = backStackEntry.arguments!!.getLong("userId")
            SignUpExtraScreen(userId = userId, navController = navController)
        }
        // WardSignup
        composable(
            route = Screen.WardSignup.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            WardSignupScreen(userId = userId, navController = navController)
        }
        // GuardianSignup
        composable(
            route = Screen.GuardianSignup.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            GuardianSignupScreen(
                userId = userId,
                navController = navController
            )
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
