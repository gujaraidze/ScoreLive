package com.example.scorelive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scorelive.presentation.navigation.BottomNavBar
import com.example.scorelive.presentation.navigation.ScoreLiveNavGraph
import com.example.scorelive.presentation.navigation.Screen
import com.example.scorelive.presentation.theme.ScoreLiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScoreLiveTheme {
                // navController lives here in MainActivity
                // shared between NavGraph and BottomNavBar
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Scaffold provides the basic screen structure
                // with a slot for bottom bar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // hide bottom nav on match detail screen
                        if (currentRoute != Screen.MatchDetail.route) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    ScoreLiveNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}