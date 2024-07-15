package com.example.hoverrobot.ui.settingsFragment

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.CommandsToRobot.CALIBRATE_IMU
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel(){

    private var _pidSettingFromRobot: MutableLiveData<PidSettings?> = MutableLiveData()
    val pidSettingFromRobot : LiveData<PidSettings?> get() = _pidSettingFromRobot

    init {
        _pidSettingFromRobot.value = null
//        ioScope.launch {                                                                          // TODO: analizar que hacer con los parametros pid
//            commsRepository.dynamicDataRobotFlow.collect {
//                _pidSettingFromRobot.postValue(it.pid)
//            }
//        }
    }

    fun setPidTunningToRobot(newTunning : PidSettings){
        if (commsRepository.connectionStateFlow.value == ConnectionStatus.CONNECTED) {
            commsRepository.sendPidParams(newTunning)
        }
    }

    fun sendCalibrateImu(){
        if (commsRepository.connectionStateFlow.value == ConnectionStatus.CONNECTED) {
            commsRepository.sendCommand(CALIBRATE_IMU.ordinal.toShort())
        }
    }

    fun getDeviceConnectedMAC(): String? {
        return commsRepository.getConnectedDevice()?.address
    }
}
