package com.app.hoverrobot.ui.settingsFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel(){

    private var _localConfigFromRobot: MutableLiveData<RobotLocalConfig> = MutableLiveData()
    val localConfigFromRobot : LiveData<RobotLocalConfig> get() = _localConfigFromRobot

    private var _statusRobot: MutableLiveData<StatusRobot> = MutableLiveData()
    val statusRobot : LiveData<StatusRobot> get() = _statusRobot

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

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                it.let {
                    _statusRobot.postValue(it.statusCode)
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
