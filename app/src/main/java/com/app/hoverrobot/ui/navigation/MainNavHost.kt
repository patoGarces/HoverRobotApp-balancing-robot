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
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.Wheel
import com.app.hoverrobot.data.utils.ToolBox.round
import com.app.hoverrobot.ui.RobotStateViewModel
import com.app.hoverrobot.ui.Screens
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnClearData
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnDatasetChange
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnPauseChange
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenWrapper
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisViewModel
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreen
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewJoystickInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawRightAction
import com.app.hoverrobot.ui.screens.statusDataScreen.StatusDataScreen
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreen
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCalibrateImu
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCleanLeftMotor
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCleanRightMotor
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnNewSettings
import kotlin.math.abs

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
        startDestination = Screens.NAVIGATION.route,
        modifier = modifier
    ) {
        composable(Screens.STATUS_DATA.route) {
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

        composable(Screens.NAVIGATION.route) {
            val actualDegrees = remember {
                derivedStateOf { robotStateViewModel.robotDynamicData?.yawAngle?.toInt() ?: 0 }
            }

            NavigationScreen(
                isRobotStabilized = robotStateViewModel.isRobotStabilized,
                isRobotConnected = robotStateViewModel.isRobotConnected,
                newPointCloudItem = robotStateViewModel.pointCloud,
                actualDegress = actualDegrees
            ) { onAction ->
                onAction.handle(robotStateViewModel)
            }
        }

        composable(Screens.SETTINGS.route) {
            SettingsScreen(
                localRobotConfig = robotStateViewModel.localConfigFromRobot,
                statusRobot = robotStateViewModel.statusRobot,
                onPidSave = { robotStateViewModel.saveLocalSettings(it) },
                onActionScreen = { onAction ->
                    onAction.handle(robotStateViewModel)
                }
            )
        }

        composable(Screens.ANALISYS.route) {
            AnalisisScreenWrapper(analisisViewModel) { onAction ->
                onAction.handle(analisisViewModel)
            }
        }
    }
}

private fun NavigationScreenAction.handle(robotStateViewModel: RobotStateViewModel) {
    when (this) {
        is OnDearmedAction -> robotStateViewModel.sendDearmedCommand()
        is OnYawLeftAction -> robotStateViewModel.sendNewMoveRelYaw(this.relativeYaw.toFloat())
        is OnYawRightAction -> robotStateViewModel.sendNewMoveRelYaw(this.relativeYaw.toFloat())
        is OnNewDragCompassInteraction -> robotStateViewModel.sendNewMoveAbsYaw(this.newDegress)
        is NavigationScreenAction.OnFixedDistance -> robotStateViewModel.sendNewMovePosition(
            abs(this.meters),
            this.meters < 0
        )

        is OnNewJoystickInteraction -> robotStateViewModel.newCoordinatesJoystick(
            (this.x * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round()
                .toInt(),
            (this.y * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round()
                .toInt()
        )
    }
}

private fun SettingsScreenActions.handle(robotStateViewModel: RobotStateViewModel) {
    when (this) {
        is OnNewSettings -> robotStateViewModel.sendNewPidSettings(this.pidSettings)
        is OnCalibrateImu -> robotStateViewModel.sendCommand(CommandsRobot.CALIBRATE_IMU)
        is OnCleanRightMotor -> robotStateViewModel.sendCommand(
            CommandsRobot.CLEAN_WHEELS, Wheel.RIGHT_WHEEL.ordinal.toFloat()
        )

        is OnCleanLeftMotor -> robotStateViewModel.sendCommand(
            CommandsRobot.CLEAN_WHEELS, Wheel.LEFT_WHEEL.ordinal.toFloat()
        )
    }
}

private fun AnalisisScreenActions.handle(analisisViewModel: AnalisisViewModel) {
    when (this) {
        is OnDatasetChange -> {
            analisisViewModel.changeSelectedDataset(this.selectedDataset)
        }
        is OnPauseChange -> {
            analisisViewModel.setPaused(this.isPaused)
        }
        is OnClearData -> {
            analisisViewModel.clearChart()
        }
    }
}
