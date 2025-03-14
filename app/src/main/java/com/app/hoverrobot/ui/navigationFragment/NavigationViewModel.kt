package com.app.hoverrobot.ui.navigationFragment

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
    private val storeSettings: StoreSettings
) : ViewModel() {
    private var _dynamicData: MutableLiveData<RobotDynamicData> = MutableLiveData()
    val dynamicData: LiveData<RobotDynamicData> get() = _dynamicData

    private var _pointCloud: MutableLiveData<PointCloudItem> = MutableLiveData()
    val pointCloud: LiveData<PointCloudItem> = _pointCloud

    private var _aggressivenessLevel: MutableLiveData<Int> = MutableLiveData(0)
    val aggressivenessLevel: LiveData<Int> = _aggressivenessLevel

    var isRobotConnected: MutableState<Boolean> = mutableStateOf(false)
        internal set

    private val _isRobotStabilized = MutableStateFlow(false)
    val isRobotStabilized: StateFlow<Boolean> = _isRobotStabilized

    init {
        ioScope.launch {
            commsRepository.connectionState.collect {
                isRobotConnected.value = it.status == StatusConnection.CONNECTED
            }
        }

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _dynamicData.postValue(it)
                _isRobotStabilized.value = it.statusCode == StatusRobot.STABILIZED

            }
        }

        ioScope.launch {
            _aggressivenessLevel.postValue(storeSettings.getAggressiveness())
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

    fun setLevelAggressiveness(level: Int) {
        _aggressivenessLevel.value = level
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