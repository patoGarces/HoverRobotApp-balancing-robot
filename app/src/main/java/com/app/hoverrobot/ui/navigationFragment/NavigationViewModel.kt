package com.app.hoverrobot.ui.navigationFragment

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.DirectionControl
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.repositories.StoreSettings
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.app.hoverrobot.data.models.Aggressiveness
import kotlinx.coroutines.runBlocking

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
    private val storeSettings: StoreSettings
) : ViewModel() {

    var dynamicData by mutableStateOf<RobotDynamicData?>(null)
        internal set

    var pointCloud = mutableStateOf<PointCloudItem>(PointCloudItem())
        internal set

    var isRobotConnected by mutableStateOf(false)
        internal set

    var isRobotStabilized by mutableStateOf(false)
        internal set

    init {
        ioScope.launch {
            commsRepository.connectionState.collect {
                isRobotConnected  = it.status == StatusConnection.CONNECTED
            }
        }

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                dynamicData = it
                isRobotStabilized = it.statusCode == StatusRobot.STABILIZED

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

    fun getAggressivenessLevel(): Aggressiveness = runBlocking { storeSettings.getAggressiveness() }

    fun setLevelAggressiveness(level: Aggressiveness) {
        ioScope.launch { storeSettings.saveAggressiveness(level) }
    }

    fun newCoordinatesJoystick(axisX: Int, axisY: Int) {
        Log.i("Aggressiveness","axisX: $axisX")
        if (isRobotConnected) {
            commsRepository.sendDirectionControl(DirectionControl(axisX.toShort(), axisY.toShort()))
        }
    }

    fun sendNewMovePosition(distanceInMts: Float, isBackward: Boolean = false) {
        val command =
            if (isBackward) CommandsRobot.MOVE_BACKWARD else CommandsRobot.MOVE_FORWARD

        if (isRobotConnected) {
            commsRepository.sendCommand(command, distanceInMts)
        }
    }

    fun sendNewMoveAbsYaw(desiredAngle: Float) {
        if (isRobotConnected) {
            commsRepository.sendCommand(CommandsRobot.MOVE_ABS_YAW, desiredAngle)
        }
    }

    fun sendNewMoveRelYaw(angle: Float) {
        if (isRobotConnected) {
            commsRepository.sendCommand(CommandsRobot.MOVE_REL_YAW, angle)
        }
    }

    fun sendDearmedCommand() {
        if (isRobotConnected) {
            commsRepository.sendCommand(CommandsRobot.DEARMED)
        }
    }
}