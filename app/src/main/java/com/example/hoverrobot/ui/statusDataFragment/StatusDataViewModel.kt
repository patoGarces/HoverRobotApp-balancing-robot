package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.Battery
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.StatusRobot
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDataViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
) : ViewModel() {

    private var _gralStatus = MutableLiveData<StatusRobot>()
    val gralStatus : LiveData<StatusRobot> = _gralStatus

    private var _motorControllerTemp = MutableLiveData(0F)
    val motorControllerTemp : LiveData<Float> = _motorControllerTemp

    private var _mainboardTemp = MutableLiveData(0F)
    val mainboardTemp : LiveData<Float> = _mainboardTemp

    private var _imuTemp = MutableLiveData(0F)
    val imuTemp : LiveData<Float> = _imuTemp

    private var _batteryStatus = MutableLiveData<Battery>()
    val batteryStatus : LiveData<Battery> = _batteryStatus

    private var _StatusConnection = MutableLiveData<StatusConnection>()
    val statusConnection : LiveData<StatusConnection> = _StatusConnection

    private var _localIp = MutableLiveData<String>()
    val localIp : LiveData<String> = _localIp

    init {
        _gralStatus.postValue(StatusRobot.INIT)
        _batteryStatus.postValue(Battery(0,0F))

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _mainboardTemp.postValue(it.tempMainboard)
                _imuTemp.postValue(it.tempImu)
                _motorControllerTemp.postValue(it.tempMcb)
                _gralStatus.postValue(StatusRobot.entries[it.statusCode])

            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _StatusConnection.postValue(it)
                _localIp.postValue(commsRepository.getLocalIp())    // TODO: mover de aca, solo provisorio.
            }
        }
    }
}