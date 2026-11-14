package com.ott.platform.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ott.platform.designsystem.OttTheme
import com.ott.platform.feature.details.impl.DetailsRoute
import com.ott.platform.feature.home.impl.HomeRoute
import com.ott.platform.feature.player.impl.PlayerRoute
import com.ott.platform.feature.search.impl.SearchRoute
import com.ott.platform.feature.watchlist.impl.WatchlistRoute
import com.ott.platform.navigation.OttRoutes

private data class TabItem(val route: String, val label: String)

@Composable
fun OttApp(
    isTv: Boolean = false,
) {
    OttTheme(darkTheme = true) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        val showBottomBar = currentRoute in setOf(OttRoutes.HOME, OttRoutes.SEARCH, OttRoutes.WATCHLIST) && !isTv

        val tabs = listOf(
            TabItem(OttRoutes.HOME, "Home"),
            TabItem(OttRoutes.SEARCH, "Search"),
            TabItem(OttRoutes.WATCHLIST, "My List"),
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(tab.label.take(1)) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = OttRoutes.HOME,
                modifier = Modifier.padding(padding),
            ) {
                composable(OttRoutes.HOME) {
                    HomeRoute(
                        isTv = isTv,
                        onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) },
                        onPlay = { navController.navigate(OttRoutes.player(it.id.value)) },
                    )
                }
                composable(OttRoutes.SEARCH) {
                    SearchRoute(onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) })
                }
                composable(OttRoutes.WATCHLIST) {
                    WatchlistRoute(onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) })
                }
                composable(
                    route = OttRoutes.DETAILS,
                    arguments = listOf(navArgument("contentId") { type = NavType.StringType }),
                ) { entry ->
                    val id = OttRoutes.decode(entry.arguments?.getString("contentId").orEmpty())
                    DetailsRoute(
                        contentId = id,
                        onPlay = { navController.navigate(OttRoutes.player(it.id.value)) },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = OttRoutes.PLAYER,
                    arguments = listOf(navArgument("contentId") { type = NavType.StringType }),
                ) { entry ->
                    val id = OttRoutes.decode(entry.arguments?.getString("contentId").orEmpty())
                    PlayerRoute(
                        contentId = id,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
