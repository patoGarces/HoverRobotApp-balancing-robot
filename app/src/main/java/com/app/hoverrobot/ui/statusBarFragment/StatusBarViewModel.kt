package com.app.hoverrobot.ui.statusBarFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.toPercentLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusBarViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _battery = MutableLiveData(Battery(false,0,0F))
    val battery : LiveData<Battery> = _battery

    private var _tempImu = MutableLiveData(0F)
    val tempImu : LiveData<Float> = _tempImu

    private var _statusRobot = MutableLiveData(StatusRobot.INIT)
    val statusRobot : LiveData<StatusRobot> = _statusRobot

    private var _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _battery.postValue(
                    Battery(
                        it.isCharging,
                        it.batVoltage.toPercentLevel(),
                        it.batVoltage
                    )
                )
                _tempImu.postValue(it.tempImu)
                _statusRobot.postValue(it.statusCode)
            }
        }


        ioScope.launch {
            commsRepository.connectionState.collect {
                _connectionState.postValue(it)
            }
        }
    }

}