package com.example.doancs.component

import PopularPlacesScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.doancs.Screen.DestinationDetailsScreen
import com.example.doancs.Screen.HomeScreen
import com.example.doancs.Screen.LocationData
import com.example.doancs.Screen.ProfileScreen
import com.example.doancs.Screen.ScheduleScreen
import com.example.doancs.Screen.SearchPlacesScreen


sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Schedule : Screen("schedule", Icons.Default.DateRange, "Schedule")
    object Find : Screen("find", Icons.Default.Search, "Find")
    object Popular : Screen("popular", Icons.Default.Star, "Popular")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object Details : Screen("details", Icons.Default.ArrowBack, "Details")
    object SignIn : Screen("signIn", Icons.Default.Person, "Sign In")
}


@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        Screen.Home, Screen.Schedule, Screen.Find, Screen.Popular, Screen.Profile
    )
    val lightBlueColor = Color(0xFF2196F3)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(icon = {
                Box(
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(
                            screen.icon,
                            contentDescription = screen.title,
                            tint = if (selected) lightBlueColor else Color.Gray
                        )
                        Text(
                            screen.title, color = if (selected) lightBlueColor else Color.Gray
                        )
                    }
                }
            }, selected = selected, onClick = {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }, colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Transparent,
                selectedTextColor = Color.Transparent,
                unselectedIconColor = Color.Transparent,
                unselectedTextColor = Color.Transparent
            )
            )
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomBar(navController = navController) }) { innerPadding ->
        NavHost(
            navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController,
                    onProfileClick = { navController.navigate(Screen.Profile.route) })
            }
            composable(Screen.Schedule.route) { ScheduleScreen() }
            composable(Screen.Find.route) { SearchPlacesScreen(navController = navController) }
            composable(Screen.Popular.route) {
                PopularPlacesScreen(
                    onBackClick = { navController.popBackStack() },
                    navController = navController
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onHomeClick = { navController.navigate(Screen.Home.route) },
                    onSignOutClick = { navController.navigate(Screen.SignIn.route) }
                )
            }
            composable(
                route = Screen.Details.route + "?name={name}&price={price}&place={place}&view={view}&description={description}",
                arguments = listOf(navArgument("name") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("place") { type = NavType.StringType },
                    navArgument("view") { type = NavType.IntType },
                    navArgument("description") { type = NavType.StringType })
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
}


@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}
