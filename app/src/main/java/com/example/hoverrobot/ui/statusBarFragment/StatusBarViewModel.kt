package com.example.hoverrobot.ui.statusBarFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.Models.comms.Battery
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.StatusEnumRobot

class StatusBarViewModel: ViewModel() {

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
    }


    fun setBatteryStatus( newStatus : Battery ){
        _battery.postValue( newStatus )
    }

    fun setConnectionStatus( status : ConnectionStatus){
        _connectionStatus.postValue(status)
    }

    fun setTempImu( temp : Float ){
        _tempImu.postValue( temp )
    }

    fun setShowDialogDevices( show : Boolean ){
        _showDialogDevices.postValue( show )
    }

    fun setStatusRobot( status : StatusEnumRobot){
        _statusRobot.postValue( status )
    }
}