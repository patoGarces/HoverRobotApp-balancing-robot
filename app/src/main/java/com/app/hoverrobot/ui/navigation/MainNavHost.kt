package com.app.hoverrobot.ui.navigation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.hoverrobot.data.models.comms.CollisionSensors
import com.app.hoverrobot.data.models.pointCloud.PointCloudItem
import com.app.hoverrobot.data.models.pointCloud.toPointCloudItem
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
                telemetryState = robotStateViewModel.telemetryData.collectAsState().value,
                networkState = robotStateViewModel.connectionNetworkState,
                robotDynamicData = robotStateViewModel.robotDynamicData,
                onOpenNetworkSettings = {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                },
                onShutdownAPS = {
                    robotStateViewModel.sendShutdownAPSCommand()
                }
            )
        }

        composable(NavigationScreens.NAVIGATION.route) {
            val actualDegrees = remember {
                derivedStateOf { robotStateViewModel.robotDynamicData?.yawAngle?.toInt() ?: 0 }
            }

            val pointCloudData by robotStateViewModel.pointCloudFlow.collectAsState(
                initial = null
            )

            val pointCloudItem = remember(pointCloudData) {
                pointCloudData?.toPointCloudItem() ?: PointCloudItem()
            }

            NavigationScreen(
                isRobotStabilized = robotStateViewModel.isRobotStabilized,
                isRobotConnected = robotStateViewModel.isRobotConnected,
                pointCloud = pointCloudItem,
                actualDegrees = actualDegrees,
                distanceSensors = robotStateViewModel.robotDynamicData?.collisionSensors ?:
                    CollisionSensors()
            ) { robotStateViewModel.onNavigationAction(it) }
        }

        composable(NavigationScreens.SETTINGS.route) {
            SettingsScreen(
                localRobotConfig = robotStateViewModel.localConfigFromRobot,
                statusRobot = robotStateViewModel.statusRobot,
                networkState = robotStateViewModel.connectionNetworkState,
                defaultSensitivityMode = robotStateViewModel.getAggressivenessLevel(),      // TODO: obtener flow del datastore directamente
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
