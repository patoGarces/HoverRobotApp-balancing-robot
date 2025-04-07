package com.app.hoverrobot.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.hoverrobot.ui.navigation.MainNavHost
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisViewModel
import com.app.hoverrobot.ui.components.BottomTabBar
import com.app.hoverrobot.ui.screens.statusBarScreen.StatusBarScreen

enum class Screens(val route: String) {
    STATUS_DATA("Status"),
    NAVIGATION("Navegación"),
    ANALISYS("Análisis"),
    SETTINGS("Configuración"),
    STATUS_BAR("status_bar")
}

@Composable
fun MainScreen(navController: NavHostController) {

    val context = LocalContext.current
    val tabs = listOf(
        Screens.STATUS_DATA,
        Screens.NAVIGATION,
        Screens.ANALISYS,
        Screens.SETTINGS,
    )

    val robotStateViewModel: RobotStateViewModel = hiltViewModel()
    val analisisViewModel: AnalisisViewModel = hiltViewModel()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screens.NAVIGATION.route
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (currentRoute != Screens.STATUS_DATA.route) {
            StatusBarScreen(
                statusRobot = robotStateViewModel.statusRobot,
                connectionState = robotStateViewModel.connectionState,
                tempImu = robotStateViewModel.robotDynamicData?.tempImu ?: 0F,
                batteryState = robotStateViewModel.batteryState
            ) {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }

        MainNavHost(
            navController = navController,
            robotStateViewModel = robotStateViewModel,
            analisisViewModel = analisisViewModel,
            context = context,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        )

        BottomTabBar(
            tabs = tabs,
            selectedIndex = selectedIndex
        ) { screen ->
            navController.navigate(screen.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}