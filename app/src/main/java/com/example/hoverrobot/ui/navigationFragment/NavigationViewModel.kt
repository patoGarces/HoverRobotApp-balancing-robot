package com.example.hoverrobot.ui.navigationFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.DirectionControl
import com.example.hoverrobot.data.models.comms.PointCloudItem
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.repositories.StoreSettings
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
    private val storeSettings: StoreSettings
): ViewModel() {

    private var _joyVisible: MutableLiveData<Boolean?> = MutableLiveData()
    val joyVisible : LiveData<Boolean?> get() = _joyVisible

    private var _dynamicData: MutableLiveData<RobotDynamicData> = MutableLiveData()
    val dynamicData : LiveData<RobotDynamicData> get() = _dynamicData

    private var _pointCloud:  MutableLiveData<List<PointCloudItem>> = MutableLiveData()
    val pointCloud: LiveData<List<PointCloudItem>> = _pointCloud

    private var _aggressivenessLevel: MutableLiveData<Int> = MutableLiveData(0)
    val aggressivenessLevel: LiveData<Int> = _aggressivenessLevel

    init {
        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                if (it == StatusConnection.CONNECTED) {
                    _joyVisible.postValue(true)
                }
                else {
                    _joyVisible.postValue(false)
                }
            }
        }

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _dynamicData.postValue(it)
            }
        }

        ioScope.launch {
            _aggressivenessLevel.postValue(storeSettings.getAggressiveness())
        }

        // TODO: aca recibo cada punto de la nube
//        ioScope.launch {
//            for (i in 0..10000) {
//                _pointCloud.postValue(
//                    listOf(
//                        PointCloudItem(
//                            x = ((Math.random()-0.5) * 10).toFloat(),
//                            y = ((Math.random()-0.5) * 10).toFloat(),
//                            ""
//                        )
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

    fun newCoordinatesJoystick(axisX: Int,axisY: Int){
        if (commsRepository.connectionStateFlow.value == StatusConnection.CONNECTED) {
            commsRepository.sendDirectionControl(DirectionControl(axisX.toShort(),axisY.toShort()))
        }
    }

    fun sendNewMovePosition(distanceInMts: Float, isBackward: Boolean = false) {
        val command = if(isBackward) CommandsRobot.COMMAND_MOVE_BACKWARD else CommandsRobot.COMMAND_MOVE_FORWARD

        if (commsRepository.connectionStateFlow.value == StatusConnection.CONNECTED) {
            commsRepository.sendCommand(command, distanceInMts)
        }
    }

    fun sendNewMoveAbsYaw(desiredAngle: Float) {
        if (commsRepository.connectionStateFlow.value == StatusConnection.CONNECTED) {
            commsRepository.sendCommand(CommandsRobot.COMMAND_MOVE_ABS_YAW, desiredAngle)
        }
    }

    fun sendNewMoveRelYaw(angle: Float) {
        if (commsRepository.connectionStateFlow.value == StatusConnection.CONNECTED) {
            commsRepository.sendCommand(CommandsRobot.COMMAND_MOVE_REL_YAW, angle)
        }
    }
}