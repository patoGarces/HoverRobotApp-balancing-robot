package com.example.hoverrobot.ui.settingsFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel(){

    private var _localConfigFromRobot: MutableLiveData<RobotLocalConfig> = MutableLiveData()
    val localConfigFromRobot : LiveData<RobotLocalConfig> get() = _localConfigFromRobot

    private val isRobotConnected: Boolean
        get() = commsRepository.connectionState.value.status == StatusConnection.CONNECTED

    init {
        ioScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it.let {
                    _localConfigFromRobot.postValue(it)
                }
            }
        }
    }

    fun sendNewPidTunning(newTunning : PidSettings): Boolean {
        return if (isRobotConnected) {
            commsRepository.sendPidParams(newTunning)
            true
        }
        else false
    }

    fun sendCommand(command: CommandsRobot,value: Float = 0F): Boolean {
        return if (isRobotConnected) {
            commsRepository.sendCommand(command,value)
            true
        }
        else false
    }
}
