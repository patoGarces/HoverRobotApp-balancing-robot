package com.example.hoverrobot.ui.statusBarFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.Models.comms.Battery
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.StatusEnumRobot
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

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    private var _showDialogDevices = MutableLiveData<Boolean?>()
    val showDialogDevices : LiveData<Boolean?> get() = _showDialogDevices

    private var _statusRobot = MutableLiveData<StatusEnumRobot?>()
    val statusRobot : LiveData<StatusEnumRobot?> get() = _statusRobot

    init {
        _showDialogDevices.postValue(false)
        _battery.postValue(Battery(0,0F,0F))
        _fpsStatus.postValue(0.0F)
        _tempImu.postValue(0F)
        _connectionStatus.postValue(ConnectionStatus.INIT)
        _statusRobot.postValue(null)

        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _battery.postValue(
                    Battery(
                        it.batPercent.toInt(),
                        it.batVoltage.toFloat() / 10,
                        it.batTemp.toFloat() / 10
                    )
                )
                _tempImu.postValue((it.tempImu.toFloat() / 10))
                _statusRobot.postValue(StatusEnumRobot.getStatusRobot(it.statusCode.toInt()))
            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _connectionStatus.postValue(it)
            }
        }
    }

    fun setShowDialogDevices( show : Boolean ){
        _showDialogDevices.postValue( show )
    }
}