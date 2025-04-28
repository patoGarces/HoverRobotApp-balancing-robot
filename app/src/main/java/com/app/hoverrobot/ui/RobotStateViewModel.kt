package com.app.hoverrobot.ui

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.hoverrobot.data.models.Aggressiveness
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.DirectionControl
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.comms.Wheel
import com.app.hoverrobot.data.models.toPercentLevel
import com.app.hoverrobot.data.repositories.APP_DEFAULT_PORT
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.repositories.IP_HOVER_ROBOT_DEFAULT
import com.app.hoverrobot.data.repositories.StoreSettings
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class RobotStateViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
    private val storeSettings: StoreSettings
) : ViewModel() {

    var localConfigFromRobot by mutableStateOf(RobotLocalConfig())
        internal set

    var statusRobot by mutableStateOf<StatusRobot>(StatusRobot.INIT)
        internal set

    var batteryState by mutableStateOf<Battery>(Battery())
        internal set

    var robotDynamicData by mutableStateOf<RobotDynamicData?>(null)
        internal set

    var connectionState by mutableStateOf(ConnectionState())
        internal set

    private val _pointCloud = mutableStateOf<PointCloudItem?>(null)
    val pointCloud: State<PointCloudItem?> = _pointCloud

    val isRobotStabilized: State<Boolean> = derivedStateOf {
        statusRobot == StatusRobot.STABILIZED
    }

    val isRobotConnected: State<Boolean> = derivedStateOf {
        connectionState.status == StatusConnection.CONNECTED
    }

    private var actualJoyPosition = DirectionControl()

    private var currentPosition = Offset(0f, 0f)
    private var lastDistance = 0f // La última distancia recibida

    init {
        commsRepository.reconnectSocket(IP_HOVER_ROBOT_DEFAULT, APP_DEFAULT_PORT)
        viewModelScope.launch {
            while (true) {
                if (isRobotConnected.value && isRobotStabilized.value) {
                    newCoordinatesJoystick(actualJoyPosition.joyAxisX, actualJoyPosition.joyAxisY)

                    Log.i("Joystick","${actualJoyPosition.joyAxisX}")
                }
                delay(50)
            }
        }

        viewModelScope.launch {
            commsRepository.connectionState.collect {
                connectionState = it
            }
        }

        viewModelScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it?.let { localConfig ->
                    localConfigFromRobot = localConfig
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

                val displacement = it.posInMeters - lastDistance
                val nextPoint = generateNextPoint(currentPosition, -it.yawAngle, displacement)
                currentPosition = nextPoint
                lastDistance = it.posInMeters
                _pointCloud.value = PointCloudItem(
                    x = nextPoint.x,
                    y = nextPoint.y
                )
            }
        }
//        viewModelScope.launch {
//            for (i in 0..5000) {
//                pointCloud.value = PointCloudItem(
//                        x = ((Math.random()-0.5) * 10).toFloat(),
//                        y = ((Math.random()-0.5) * 10).toFloat(),
//                        ""
//                    )
//                delay(100)
//            }
//        }
    }

    fun onNavigationAction(action: NavigationScreenAction) {
        when (action) {
            is OnDearmedAction -> sendDearmedCommand()
            is OnYawLeftAction -> sendNewMoveRelYaw(action.relativeYaw.toFloat())
            is OnYawRightAction -> sendNewMoveRelYaw(action.relativeYaw.toFloat())
            is OnNewDragCompassInteraction -> sendNewMoveAbsYaw(action.newDegress)
            is NavigationScreenAction.OnFixedDistance -> sendNewMovePosition(
                abs(action.meters),
                action.meters < 0
            )

            is OnNewJoystickInteraction -> {
                actualJoyPosition = DirectionControl(action.x.toShort(), action.y.toShort())
            }
        }
    }

    fun onSettingsScreenActions(action: SettingsScreenActions) {
        when (action) {
            is OnNewSettings -> sendNewPidSettings(action.pidSettings)
            is OnCalibrateImu -> sendCommand(CommandsRobot.CALIBRATE_IMU)
            is OnCleanRightMotor -> sendCommand(
                CommandsRobot.CLEAN_WHEELS, Wheel.RIGHT_WHEEL.ordinal.toFloat()
            )

            is OnCleanLeftMotor -> sendCommand(
                CommandsRobot.CLEAN_WHEELS, Wheel.LEFT_WHEEL.ordinal.toFloat()
            )
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

    private fun generateNextPoint(
        previousPosition: Offset,
        yawDegrees: Float,
        deltaDistance: Float // Distancia recorrida desde el punto anterior
    ): Offset {
        val yawRadians = Math.toRadians(yawDegrees.toDouble())

        // Calcular el desplazamiento en los ejes X y Y
        val dx = (deltaDistance * sin(yawRadians)).toFloat()
        val dy = (deltaDistance * cos(yawRadians)).toFloat()

        return previousPosition + Offset(dx, dy)
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

    private fun sendNewMovePosition(distanceInMts: Float, isBackward: Boolean = false) {
        val command =
            if (isBackward) CommandsRobot.MOVE_BACKWARD else CommandsRobot.MOVE_FORWARD

        if (isRobotConnected.value) {
            commsRepository.sendCommand(command, distanceInMts)
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