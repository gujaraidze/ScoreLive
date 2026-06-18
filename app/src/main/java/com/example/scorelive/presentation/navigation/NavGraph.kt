package com.example.scorelive.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.scorelive.presentation.competition.CompetitionScreen
import com.example.scorelive.presentation.favorites.FavoritesScreen
import com.example.scorelive.presentation.home.HomeScreen
import com.example.scorelive.presentation.matchdetail.MatchDetailScreen
import com.example.scorelive.presentation.theme.BackgroundCard
import com.example.scorelive.presentation.theme.BottomNavSelected
import com.example.scorelive.presentation.theme.BottomNavUnselected
import androidx.navigation.compose.rememberNavController

// all screen routes as constants — no typos possible
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Competition : Screen("competition")
    object Favorites : Screen("favorites")
    object MatchDetail : Screen("match_detail/{matchId}") {
        fun createRoute(matchId: Int) = "match_detail/$matchId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Competition, "Competition", Icons.Default.Search),
    BottomNavItem(Screen.Favorites, "Live", Icons.Default.PlayArrow),
    BottomNavItem(Screen.Favorites, "Favorites", Icons.Default.Star)
)

@Composable
fun ScoreLiveNavGraph(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onMatchClicked = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }

        composable(Screen.Competition.route) {
            CompetitionScreen(
                onMatchClicked = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onMatchClicked = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }

        composable(
            route = Screen.MatchDetail.route,
            arguments = listOf(
                navArgument("matchId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            MatchDetailScreen(
                matchId = matchId,
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = BackgroundCard) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BottomNavSelected,
                    selectedTextColor = BottomNavSelected,
                    unselectedIconColor = BottomNavUnselected,
                    unselectedTextColor = BottomNavUnselected,
                    indicatorColor = BackgroundCard
                )
            )
        }
    }
}