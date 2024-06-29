package com.example.hoverrobot.ui.controlFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _joyVisible : MutableLiveData<Boolean?> = MutableLiveData()
    val joyVisible : LiveData<Boolean?> get() = _joyVisible

    private var _dynamicData : MutableLiveData<RobotDynamicData> = MutableLiveData()
    val dynamicData : LiveData<RobotDynamicData> get() = _dynamicData

    init {
        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                if (it == ConnectionStatus.CONNECTED) {
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
    }
    fun newCoordinatesJoystick(newAxisControl: AxisControl){
        if (commsRepository.connectionStateFlow.value == ConnectionStatus.CONNECTED) {
            commsRepository.sendJoystickUpdate(newAxisControl)
        }
    }
}