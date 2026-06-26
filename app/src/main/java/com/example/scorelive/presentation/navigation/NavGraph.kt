package com.example.scorelive.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scorelive.presentation.competition.CompetitionScreen
import com.example.scorelive.presentation.competition.CompetitionDetailScreen
import com.example.scorelive.presentation.favorites.FavoritesScreen
import com.example.scorelive.presentation.home.HomeScreen
import com.example.scorelive.presentation.matchdetail.MatchDetailScreen
import com.example.scorelive.presentation.search.SearchScreen
import com.example.scorelive.presentation.theme.BackgroundCard
import com.example.scorelive.presentation.theme.BottomNavSelected
import com.example.scorelive.presentation.theme.BottomNavUnselected

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Competition : Screen("competition")
    object CompetitionDetail : Screen("competition_detail/{leagueId}/{leagueName}/{logoUrl}/{country}") {
        fun createRoute(leagueId: Int, leagueName: String, logoUrl: String, country: String) =
            "competition_detail/$leagueId/${java.net.URLEncoder.encode(leagueName, "UTF-8")}/${java.net.URLEncoder.encode(logoUrl, "UTF-8")}/${java.net.URLEncoder.encode(country, "UTF-8")}"
    }
    object Search : Screen("search")
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

// 3 bottom nav tabs: Home, Competition, Favorites
val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Competition, "Competition", Icons.Default.SportsSoccer),
    BottomNavItem(Screen.Favorites, "Favorites", Icons.Default.Star)
)

@Composable
fun ScoreLiveNavGraph(
    navController: NavHostController = rememberNavController(),
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
                },
                onSearchClicked = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Competition.route) {
            CompetitionScreen(
                onLeagueClicked = { league ->
                    navController.navigate(
                        Screen.CompetitionDetail.createRoute(
                            leagueId = league.id,
                            leagueName = league.name,
                            logoUrl = league.logoUrl,
                            country = league.country
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.CompetitionDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("leagueId") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("leagueName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("logoUrl") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("country") { type = androidx.navigation.NavType.StringType },
            )
        ) { backStackEntry ->
            val leagueId = backStackEntry.arguments?.getInt("leagueId") ?: 0
            val leagueName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("leagueName") ?: "", "UTF-8")
            val logoUrl = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("logoUrl") ?: "", "UTF-8")
            val country = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("country") ?: "", "UTF-8")
            CompetitionDetailScreen(
                leagueId = leagueId,
                leagueName = leagueName,
                logoUrl = logoUrl,
                country = country,
                onBackClicked = { navController.popBackStack() },
                onMatchClicked = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onMatchClicked = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                },
                onBackClicked = { navController.popBackStack() }
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