package com.app.hoverrobot.ui.navigation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.hoverrobot.ui.RobotStateViewModel
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenWrapper
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisViewModel
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreen
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreen
import com.app.hoverrobot.ui.screens.statusDataScreen.StatusDataScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    robotStateViewModel: RobotStateViewModel,
    analisisViewModel: AnalisisViewModel,
    context: Context,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreens.NAVIGATION.route,
        modifier = modifier
    ) {
        composable(NavigationScreens.STATUS_DATA.route) {
            StatusDataScreen(
                statusRobot = robotStateViewModel.statusRobot,
                statusConnection = robotStateViewModel.connectionState.status,
                defaultAggressiveness = robotStateViewModel.getAggressivenessLevel().ordinal,
                mainboardTemp = robotStateViewModel.robotDynamicData?.tempMainboard ?: 0F,
                motorControllerTemp = robotStateViewModel.robotDynamicData?.tempMcb ?: 0F,
                imuTemp = robotStateViewModel.robotDynamicData?.tempImu ?: 0F,
                localIp = robotStateViewModel.connectionState.ip,
                onOpenNetworkSettings = {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                },
                onAggressivenessChange = {
                    robotStateViewModel.setLevelAggressiveness(it)
                }
            )
        }

        composable(NavigationScreens.NAVIGATION.route) {
            val actualDegrees = remember {
                derivedStateOf { robotStateViewModel.robotDynamicData?.yawAngle?.toInt() ?: 0 }
            }

            NavigationScreen(
                isRobotStabilized = robotStateViewModel.isRobotStabilized,
                isRobotConnected = robotStateViewModel.isRobotConnected,
                newPointCloudItem = robotStateViewModel.pointCloud,
                actualDegrees = actualDegrees
            ) { robotStateViewModel.onNavigationAction(it) }
        }

        composable(NavigationScreens.SETTINGS.route) {
            SettingsScreen(
                localRobotConfig = robotStateViewModel.localConfigFromRobot,
                statusRobot = robotStateViewModel.statusRobot,
                serverRobotAddress = robotStateViewModel.serverAddressRobot,
                onPidSave = { robotStateViewModel.saveLocalSettings(it) },
                onActionScreen = { robotStateViewModel.onSettingsScreenActions(it) }
            )
        }

        composable(NavigationScreens.ANALISYS.route) {
            AnalisisScreenWrapper(analisisViewModel) {
                analisisViewModel.onAnalisisScreenActions(it)
            }
        }
    }
}
