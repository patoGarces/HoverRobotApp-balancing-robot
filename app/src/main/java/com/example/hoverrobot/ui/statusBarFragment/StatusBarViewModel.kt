package com.example.hoverrobot.ui.statusBarFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.Battery
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.StatusRobot
import com.example.hoverrobot.data.utils.ToolBox.Companion.toPercentLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusBarViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _battery = MutableLiveData<Battery>()
    val battery : LiveData<Battery> = _battery

    private var _fpsStatus = MutableLiveData<Float>()
    var fpsStatus : LiveData<Float> = _fpsStatus

    private var _tempImu = MutableLiveData<Float>()
    val tempImu : LiveData<Float> = _tempImu

    private var _StatusConnection = MutableLiveData<StatusConnection>()
    val statusConnection : LiveData<StatusConnection> = _StatusConnection

    private var _statusRobot = MutableLiveData<StatusRobot>()
    val statusRobot : LiveData<StatusRobot> get() = _statusRobot

    init {
        _battery.postValue(Battery(0,0F))
        _fpsStatus.postValue(0.0F)
        _tempImu.postValue(0F)
        _StatusConnection.postValue(StatusConnection.INIT)
        _statusRobot.postValue(StatusRobot.INIT)

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _battery.postValue(
                    Battery(
                        it.batVoltage.toPercentLevel(),
                        it.batVoltage
                    )
                )
                _tempImu.postValue(it.tempImu)
                _statusRobot.postValue(StatusRobot.getStatusRobot(it.statusCode))
            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _StatusConnection.postValue(it)
            }
        }
    }

}