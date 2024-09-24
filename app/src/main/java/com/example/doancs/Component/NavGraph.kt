package com.example.doancs.navigation

import PopularPlacesScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.doancs.Screen.DestinationDetailsScreen
import com.example.doancs.Screen.ForgotPasswordScreen
import com.example.doancs.Screen.HomeScreen
import com.example.doancs.Screen.LocationData
import com.example.doancs.Screen.ProfileScreen
import com.example.doancs.Screen.SignInScreen
import com.example.doancs.Screen.SignUpScreen
import com.example.doancs.component.MainScreen


sealed class Screen(val route: String) {
    data object SignIn : Screen("signIn")
    data object Home : Screen("home")
    data object SignUp : Screen("signUp")
    data object ForgotPassword : Screen("forgotPassword")
    data object Main : Screen("main")
    data object Details : Screen("details")
    data object Profile : Screen("profile")
    data object Popular : Screen("popular")
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
    ) {
        composable(route = Screen.SignIn.route) {
            SignInScreen(
                onSignInClick = { navController.navigate(Screen.Main.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onSignInClick = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable(route = Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onSignInClick = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable(route = Screen.Main.route) {
            MainScreen()
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onSignOutClick = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable(route = Screen.Popular.route) {
            PopularPlacesScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            route = Screen.Details.route + "?name={name}&price={price}&place={place}&view={view}&description={description}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("price") { type = NavType.StringType },
                navArgument("place") { type = NavType.StringType },
                navArgument("view") { type = NavType.IntType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val price = backStackEntry.arguments?.getString("price") ?: ""
            val place = backStackEntry.arguments?.getString("place") ?: ""
            val view = backStackEntry.arguments?.getInt("view") ?: 0
            val description = backStackEntry.arguments?.getString("description") ?: ""

            val destinationData = LocationData(
                name = name,
                view = view,
                place = place,
                description = description,
                price = price
            )

            DestinationDetailsScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController,
                locationData = destinationData
            )
        }
    }
}
