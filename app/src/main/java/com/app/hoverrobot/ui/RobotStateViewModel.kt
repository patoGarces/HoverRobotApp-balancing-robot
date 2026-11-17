package com.app.hoverrobot.ui

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.hoverrobot.data.models.Aggressiveness
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.DirectionControl
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.comms.Wheel
import com.app.hoverrobot.data.models.toPercentLevel
import com.app.hoverrobot.data.repositories.APP_DEFAULT_PORT
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.repositories.IP_ADDRESS_CLIENT_NULL
import com.app.hoverrobot.data.repositories.IP_ADDRESS_CLIENT_RASPI_DEFAULT
import com.app.hoverrobot.data.repositories.IP_ADDRESS_CLIENT_ROBOT_DEFAULT
import com.app.hoverrobot.data.repositories.StoreSettings
import com.app.hoverrobot.data.repositories.WEBSOCKET_DEFAULT_PORT
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.round
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewJoystickInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawRightAction
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCalibrateImu
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCleanLeftMotor
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnCleanRightMotor
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnNewSettings
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnPidAngleTest
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnReconnectToRaspi
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnReconnectToRobot
import com.app.hoverrobot.ui.screens.settingsScreen.SettingsScreenActions.OnRefreshLocalConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RobotStateViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
    private val storeSettings: StoreSettings
) : ViewModel() {
    var localConfigFromRobot by mutableStateOf(RobotLocalConfig())
        internal set

    var statusRobot by mutableStateOf(StatusRobot.INIT)
        internal set

    var batteryState by mutableStateOf(Battery())
        internal set

    var robotDynamicData by mutableStateOf<RobotDynamicData?>(null)
        internal set

    var connectionNetworkState by mutableStateOf(NetworkState())
        internal set

    val pointCloudFlow = commsRepository.pointCloudFlow

    val telemetryData = commsRepository.raspiTelemetryData

    val isRobotStabilized: State<Boolean> = derivedStateOf {
        statusRobot == StatusRobot.STABILIZED
    }

    val isRobotConnected: State<Boolean> = derivedStateOf {
        connectionNetworkState.statusRobotClient.status == StatusConnection.CONNECTED
    }

    private var actualJoyPosition = DirectionControl()

    private var touchUpJoystick: Boolean = false

    private val DirectionControl.isNotZero: Boolean
        get() = this.joyAxisX.toInt() != 0 || this.joyAxisY.toInt() != 0


    init {
        viewModelScope.launch {
            val ipRobot = storeSettings.getIpRobot() ?: IP_ADDRESS_CLIENT_ROBOT_DEFAULT
            if (ipRobot != IP_ADDRESS_CLIENT_NULL) {
                commsRepository.reconnectRobotSocket(ipRobot, APP_DEFAULT_PORT)
            }

            val ipRaspi = storeSettings.getIpRaspi() ?: IP_ADDRESS_CLIENT_RASPI_DEFAULT
            if (ipRaspi != IP_ADDRESS_CLIENT_NULL) {
                Log.i("raspiWS", "conectando a $ipRaspi con puerto: $WEBSOCKET_DEFAULT_PORT")
                commsRepository.reconnectRaspiSocket(ipRaspi, WEBSOCKET_DEFAULT_PORT)
            }
        }

        viewModelScope.launch {
            while (true) {
                if (isRobotConnected.value &&
                    isRobotStabilized.value &&
                    (actualJoyPosition.isNotZero || touchUpJoystick)) {

                    newCoordinatesJoystick(actualJoyPosition.joyAxisX, actualJoyPosition.joyAxisY)
                    touchUpJoystick = false
                }
                delay(25)
            }
        }

        viewModelScope.launch {
            commsRepository.connectionNetworkState.collect {
                connectionNetworkState = it
            }
        }

        viewModelScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it?.let { localConfig ->
                    if (localConfig != localConfigFromRobot) {
                        localConfigFromRobot = localConfig
                    }
                }
            }
        }

        viewModelScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                robotDynamicData = it
                it.let {
                    statusRobot = it.statusCode

                    batteryState = Battery(
                        it.isCharging,
                        it.batVoltage.toPercentLevel(),
                        it.batVoltage
                    )
                }
            }
        }
    }

    fun onNavigationAction(action: NavigationScreenAction) {
        when (action) {
            is OnDearmedAction -> sendDearmedCommand()
            is OnYawLeftAction -> sendNewMoveRelYaw(action.relativeYaw.toFloat())
            is OnYawRightAction -> sendNewMoveRelYaw(action.relativeYaw.toFloat())
            is OnNewDragCompassInteraction -> sendNewMoveAbsYaw(action.newDegress)
            is NavigationScreenAction.OnFixedDistance -> sendNewMovePosition(action.meters)
            is OnNewJoystickInteraction -> {
                actualJoyPosition = DirectionControl(action.x.toShort(), action.y.toShort())
                touchUpJoystick = true
            }
        }
    }

    fun onSettingsScreenActions(action: SettingsScreenActions) {
        viewModelScope.launch {
            when (action) {
                is OnNewSettings -> sendNewPidSettings(action.pidSettings)
                is OnRefreshLocalConfig -> sendCommand(CommandsRobot.GET_PARAMS_SETTINGS)
                is OnCalibrateImu -> sendCommand(CommandsRobot.CALIBRATE_IMU)
                is OnCleanRightMotor -> sendCommand(
                    CommandsRobot.CLEAN_WHEELS, Wheel.RIGHT_WHEEL.ordinal.toFloat()
                )

                is OnCleanLeftMotor -> sendCommand(
                    CommandsRobot.CLEAN_WHEELS, Wheel.LEFT_WHEEL.ordinal.toFloat()
                )

                is OnPidAngleTest -> sendCommand(CommandsRobot.PID_ANGLE_TEST)
                is OnReconnectToRobot -> {
                    storeSettings.setIpRobot(action.ip)
                    if (action.ip != IP_ADDRESS_CLIENT_NULL) {
                        commsRepository.reconnectRobotSocket(
                            action.ip,
                            APP_DEFAULT_PORT
                        )
                    }
                }

                is OnReconnectToRaspi -> {
                    storeSettings.setIpRaspi(action.ip)
                    if (action.ip != IP_ADDRESS_CLIENT_NULL) {
                        commsRepository.reconnectRaspiSocket(
                            action.ip,
                            WEBSOCKET_DEFAULT_PORT
                        )
                    }
                }
            }
        }
    }

    fun saveLocalSettings(newSetting: PidSettings): Boolean {
        return if (sendNewPidSettings(newSetting))
            sendCommand(CommandsRobot.SAVE_PARAMS_SETTINGS)
        else false
    }

    fun getAggressivenessLevel(): Aggressiveness = runBlocking { storeSettings.getAggressiveness() }

    fun setLevelAggressiveness(level: Aggressiveness) {
        viewModelScope.launch { storeSettings.saveAggressiveness(level) }
    }

    private fun sendNewPidSettings(newSetting: PidSettings): Boolean {
        return if (isRobotConnected.value) {
            commsRepository.sendPidParams(newSetting)
            true
        } else false
    }

    private fun sendCommand(command: CommandsRobot, value: Float = 0F): Boolean {
        return if (isRobotConnected.value) {
            commsRepository.sendCommand(command, value)
            true
        } else false
    }

    private fun newCoordinatesJoystick(actualX: Short, actualY: Short) {
        if (isRobotConnected.value) {
            val axisX = (actualX * getAggressivenessLevel().normalizedFactor).round()
                .toInt()
            val axisY = (actualY * getAggressivenessLevel().normalizedFactor).round()
                .toInt()
            commsRepository.sendDirectionControl(DirectionControl(axisX.toShort(), axisY.toShort()))
        }
    }

    private fun sendNewMovePosition(distanceInMts: Float) {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.MOVE_DISTANCE, distanceInMts)
        }
    }

    private fun sendNewMoveAbsYaw(desiredAngle: Float) {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.MOVE_ABS_YAW, desiredAngle)
        }
    }

    private fun sendNewMoveRelYaw(angle: Float) {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.MOVE_REL_YAW, angle)
        }
    }

    private fun sendDearmedCommand() {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.DEARMED)
        }
    }
}