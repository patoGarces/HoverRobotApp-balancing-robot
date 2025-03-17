package com.app.hoverrobot.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.Aggressiveness
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.DirectionControl
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.toPercentLevel
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.repositories.StoreSettings
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var statusRobot by mutableStateOf<StatusRobot>(StatusRobot.INIT)
        internal set

    var batteryState by mutableStateOf<Battery>(Battery())
        internal set

    var robotDynamicData by mutableStateOf<RobotDynamicData?>(null)
        internal set

    var connectionState by mutableStateOf(ConnectionState())
        internal set

    var pointCloud = mutableStateOf<PointCloudItem>(PointCloudItem())
        internal set

    val isRobotStabilized: State<Boolean> = derivedStateOf {
        statusRobot == StatusRobot.STABILIZED
    }

    val isRobotConnected: State<Boolean> = derivedStateOf {
        connectionState.status == StatusConnection.CONNECTED
    }

    init {
        ioScope.launch {
            commsRepository.connectionState.collect {
                connectionState = it
            }
        }

        ioScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it?.let { localConfig ->
                    localConfigFromRobot = localConfig
                }
            }
        }

        ioScope.launch {
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

//        // TODO: aca recibo cada punto de la nube
//        ioScope.launch {
//            for (i in 0..10000) {
//                _pointCloud.postValue(
//                    PointCloudItem(
//                        x = ((Math.random()-0.5) * 10).toFloat(),
//                        y = ((Math.random()-0.5) * 10).toFloat(),
//                        ""
//                    )
//                )
//                delay(100)
//            }
//        }
    }


    fun saveLocalSettings(newSetting: PidSettings): Boolean {
        return if (sendNewPidSettings(newSetting))
            sendCommand(CommandsRobot.SAVE_PARAMS_SETTINGS)
        else false
    }

    fun sendNewPidSettings(newSetting: PidSettings): Boolean {
        return if (isRobotConnected.value) {
            commsRepository.sendPidParams(newSetting)
            true
        } else false
    }

    fun sendCommand(command: CommandsRobot, value: Float = 0F): Boolean {
        return if (isRobotConnected.value) {
            commsRepository.sendCommand(command, value)
            true
        } else false
    }

    fun getAggressivenessLevel(): Aggressiveness = runBlocking { storeSettings.getAggressiveness() }

    fun setLevelAggressiveness(level: Aggressiveness) {
        ioScope.launch { storeSettings.saveAggressiveness(level) }
    }

    fun newCoordinatesJoystick(axisX: Int, axisY: Int) {
        if (isRobotConnected.value) {
            commsRepository.sendDirectionControl(DirectionControl(axisX.toShort(), axisY.toShort()))
        }
    }

    fun sendNewMovePosition(distanceInMts: Float, isBackward: Boolean = false) {
        val command =
            if (isBackward) CommandsRobot.MOVE_BACKWARD else CommandsRobot.MOVE_FORWARD

        if (isRobotConnected.value) {
            commsRepository.sendCommand(command, distanceInMts)
        }
    }

    fun sendNewMoveAbsYaw(desiredAngle: Float) {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.MOVE_ABS_YAW, desiredAngle)
        }
    }

    fun sendNewMoveRelYaw(angle: Float) {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.MOVE_REL_YAW, angle)
        }
    }

    fun sendDearmedCommand() {
        if (isRobotConnected.value) {
            commsRepository.sendCommand(CommandsRobot.DEARMED)
        }
    }
}