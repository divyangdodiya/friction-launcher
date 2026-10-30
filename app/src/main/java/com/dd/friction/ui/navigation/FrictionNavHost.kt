package com.dd.friction.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dd.friction.ui.gate.GateScreen
import com.dd.friction.ui.home.HomeScreen
import com.dd.friction.ui.menu.MenuScreen
import com.dd.friction.ui.settings.SettingsScreen

@Composable
fun FrictionNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val backToHome: () -> Unit = { navController.popBackStack(HomeRoute, inclusive = false) }

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        // No animations: instant, plain, nothing pleasing about moving around.
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<HomeRoute> {
            HomeScreen(onOpenMenu = { navController.navigate(MenuRoute) })
        }
        composable<MenuRoute> {
            MenuScreen(
                onOpenGate = { app -> navController.navigate(GateRoute(app.packageName, app.activityName)) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onLaunched = backToHome,
            )
        }
        composable<GateRoute> {
            GateScreen(onExit = backToHome)
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
