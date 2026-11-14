package com.ott.platform.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
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
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttTheme
import com.ott.platform.feature.details.impl.DetailsRoute
import com.ott.platform.feature.home.impl.HomeRoute
import com.ott.platform.feature.player.impl.PlayerRoute
import com.ott.platform.feature.search.impl.SearchRoute
import com.ott.platform.feature.watchlist.impl.WatchlistRoute
import com.ott.platform.navigation.OttRoutes
import com.ott.platform.ui.components.OttBottomMenu
import com.ott.platform.ui.components.OttMenuItem
import com.ott.platform.ui.components.OttPhoneTopBar
import com.ott.platform.ui.components.OttTopMenu

private val MainTabs = listOf(
    OttMenuItem(OttRoutes.HOME, "Home"),
    OttMenuItem(OttRoutes.SEARCH, "Search"),
    OttMenuItem(OttRoutes.WATCHLIST, "Library"),
)

@Composable
fun OttApp(
    isTv: Boolean = false,
) {
    OttTheme(darkTheme = true) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        val onMainTab = currentRoute in setOf(OttRoutes.HOME, OttRoutes.SEARCH, OttRoutes.WATCHLIST)
        val showTvTopMenu = isTv && onMainTab
        val showPhoneChrome = !isTv && onMainTab
        val isPlayer = currentRoute == OttRoutes.PLAYER

        fun navigateToTab(item: OttMenuItem) {
            navController.navigate(item.id) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Scaffold(
            containerColor = OttColors.Background,
            contentWindowInsets = when {
                isPlayer || currentRoute == OttRoutes.SPLASH -> WindowInsets(0, 0, 0, 0)
                // Top/bottom chrome apply their own system-bar padding.
                showPhoneChrome || showTvTopMenu -> WindowInsets(0, 0, 0, 0)
                else -> WindowInsets.safeDrawing
            },
            topBar = {
                when {
                    showTvTopMenu -> OttTopMenu(
                        items = MainTabs,
                        selectedId = currentRoute,
                        isTv = true,
                        onItemSelected = ::navigateToTab,
                        onSearchClick = {
                            navigateToTab(OttMenuItem(OttRoutes.SEARCH, "Search"))
                        },
                    )
                    showPhoneChrome -> OttPhoneTopBar()
                }
            },
            bottomBar = {
                if (showPhoneChrome) {
                    OttBottomMenu(
                        items = MainTabs,
                        selectedId = currentRoute,
                        onItemSelected = ::navigateToTab,
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OttColors.Background)
                    .padding(padding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = OttRoutes.SPLASH,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(OttRoutes.SPLASH) {
                        SplashRoute(
                            onFinished = {
                                navController.navigate(OttRoutes.HOME) {
                                    popUpTo(OttRoutes.SPLASH) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(OttRoutes.HOME) {
                        HomeRoute(
                            isTv = isTv,
                            onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) },
                            onPlay = { navController.navigate(OttRoutes.player(it.id.value)) },
                        )
                    }
                    composable(OttRoutes.SEARCH) {
                        SearchRoute(
                            isTv = isTv,
                            onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) },
                        )
                    }
                    composable(OttRoutes.WATCHLIST) {
                        WatchlistRoute(
                            isTv = isTv,
                            onOpenDetails = { navController.navigate(OttRoutes.details(it.id.value)) },
                        )
                    }
                    composable(
                        route = OttRoutes.DETAILS,
                        arguments = listOf(navArgument("contentId") { type = NavType.StringType }),
                    ) { entry ->
                        val id = OttRoutes.decode(entry.arguments?.getString("contentId").orEmpty())
                        DetailsRoute(
                            contentId = id,
                            isTv = isTv,
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
                            isTv = isTv,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
