package com.app.hoverrobot

import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.Wheel
import com.app.hoverrobot.data.utils.ToolBox.round
import com.app.hoverrobot.ui.RobotStateViewModel
import com.app.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.app.hoverrobot.ui.navigationScreen.NavigationScreen
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction.OnNewJoystickInteraction
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.navigationScreen.NavigationScreenAction.OnYawRightAction
import com.app.hoverrobot.ui.settingsScreen.SettingsScreenActions.OnCalibrateImu
import com.app.hoverrobot.ui.settingsScreen.SettingsScreenActions.OnCleanLeftMotor
import com.app.hoverrobot.ui.settingsScreen.SettingsScreenActions.OnCleanRightMotor
import com.app.hoverrobot.ui.settingsScreen.SettingsScreenActions.OnNewSettings
import com.app.hoverrobot.ui.settingsScreen.SettingsScreen
import com.app.hoverrobot.ui.statusBarScreen.StatusBarScreen
import com.app.hoverrobot.ui.statusDataScreen.StatusDataScreen
import kotlin.math.abs

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

    val robotStateViewModel: RobotStateViewModel = viewModel()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screens.NAVIGATION.route
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

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

        NavHost(
            navController = navController,
            startDestination = Screens.NAVIGATION.route,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
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
                    onOpenNetworkSettings = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onAggressivenessChange = { robotStateViewModel.setLevelAggressiveness(it) }
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
                    when (onAction) {
                        is OnDearmedAction -> robotStateViewModel.sendDearmedCommand()
                        is OnYawLeftAction -> robotStateViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
                        is OnYawRightAction -> robotStateViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
                        is OnNewDragCompassInteraction -> robotStateViewModel.sendNewMoveAbsYaw(
                            onAction.newDegress
                        )

                        is NavigationScreenAction.OnFixedDistance -> {
                            robotStateViewModel.sendNewMovePosition(
                                abs(onAction.meters),
                                onAction.meters < 0
                            )
                        }

                        is OnNewJoystickInteraction -> {
                            robotStateViewModel.newCoordinatesJoystick(
                                (onAction.x * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round()
                                    .toInt(),
                                (onAction.y * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round()
                                    .toInt()
                            )
                        }
                    }
                }
            }
            composable(Screens.SETTINGS.route) {
                SettingsScreen(
                    localRobotConfig = robotStateViewModel.localConfigFromRobot,
                    statusRobot = robotStateViewModel.statusRobot,
                    onPidSave = { robotStateViewModel.saveLocalSettings(it) },
                    onActionScreen = { onAction ->
                        when (onAction) {
                            is OnNewSettings -> robotStateViewModel.sendNewPidSettings(onAction.pidSettings)
                            is OnCalibrateImu -> robotStateViewModel.sendCommand(CommandsRobot.CALIBRATE_IMU)
                            is OnCleanRightMotor -> robotStateViewModel.sendCommand(
                                CommandsRobot.CLEAN_WHEELS,
                                Wheel.RIGHT_WHEEL.ordinal.toFloat()
                            )

                            is OnCleanLeftMotor -> robotStateViewModel.sendCommand(
                                CommandsRobot.CLEAN_WHEELS,
                                Wheel.LEFT_WHEEL.ordinal.toFloat()
                            )
                        }
                    }
                )
            }
            composable(Screens.ANALISYS.route) {
                AndroidView(
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = View.generateViewId()
                            (context as? AppCompatActivity)?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(this.id, AnalisisFragment())
                                ?.commit()
                        }
                    }
                )
            }
        }

        TabRow(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .height(35.dp)
                .align(Alignment.CenterHorizontally),
            containerColor = Color.Transparent,
            contentColor = Color.Red,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .height(2.dp)
                        .background(Color.Red)
                )
            },
            divider = {},
            selectedTabIndex = selectedIndex,
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clickable { navController.navigate(tabs[index].route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.route,
                        color = if (index == selectedIndex) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
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