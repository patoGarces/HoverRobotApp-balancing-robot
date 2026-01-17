package com.app.hoverrobot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.hoverrobot.BuildConfig.FIRMWARE_VERSION_COMPATIBLE
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.components.BottomTabBar
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.SingleDialog
import com.app.hoverrobot.ui.navigation.MainNavHost
import com.app.hoverrobot.ui.navigation.NavigationScreens
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisViewModel
import com.app.hoverrobot.ui.screens.statusBarScreen.StatusBarScreen

@Composable
fun MainScreen(navController: NavHostController) {

    val context = LocalContext.current
    val tabs = listOf(
        NavigationScreens.STATUS_DATA,
        NavigationScreens.NAVIGATION,
        NavigationScreens.ANALISYS,
        NavigationScreens.SETTINGS,
    )
    val robotStateViewModel: RobotStateViewModel = hiltViewModel()
    val analisisViewModel: AnalisisViewModel = hiltViewModel()
    val currentRoute = currentRoute(navController)
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    var showFirmwareVersionDialog by remember { mutableStateOf(false) }

    val firmwareVersionReceived: Int = robotStateViewModel.localConfigFromRobot.versionFirmware
    LaunchedEffect(firmwareVersionReceived) {
        if (firmwareVersionReceived != 0 && firmwareVersionReceived != FIRMWARE_VERSION_COMPATIBLE.toInt()) {       // TODO: deberia ser null localConfigFromRobot hasta recibir la config
           showFirmwareVersionDialog = true
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (currentRoute != NavigationScreens.STATUS_DATA.route) {
            StatusBarScreen(
                statusRobot = robotStateViewModel.statusRobot,
                networkState = robotStateViewModel.connectionNetworkState,
                tempImu = robotStateViewModel.robotDynamicData?.temperatures?.tempImu ?: 0F,
                batteryState = robotStateViewModel.batteryState
            ) { navController.goToScreen(NavigationScreens.STATUS_DATA) }
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
            navController.goToScreen(screen)
        }
    }

    if (showFirmwareVersionDialog) {
        SingleDialog(
            title = R.string.firmware_dialog_title,
            description = R.string.firmware_dialog_description
        ) { showFirmwareVersionDialog = false }
    }
}

private fun NavHostController.goToScreen(screen: NavigationScreens) {
    val navController = this
    navController.navigate(screen.route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String {
    return navController.currentBackStackEntryAsState().value?.destination?.route
        ?: NavigationScreens.NAVIGATION.route
}

@CustomPreview
@Composable
private fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}