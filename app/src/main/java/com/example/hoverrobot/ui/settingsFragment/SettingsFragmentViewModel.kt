package com.example.hoverrobot.ui.settingsFragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.ToolBox.Companion.ioScope
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

        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _pidSettingFromRobot.postValue(it.pid)
            }
        }
    }

    fun setPidTunningToRobot(newTunning : PidSettings){
        if (commsRepository.connectionStateFlow.value == ConnectionStatus.CONNECTED) {
            commsRepository.sendPidParams(newTunning)
        } else {
            Log.d("activity", "No se puede enviar configuración")
        }
    }

    fun setPidTunningfromRobot(newTunning : PidSettings){
        _pidSettingFromRobot.postValue( newTunning )
    }
}