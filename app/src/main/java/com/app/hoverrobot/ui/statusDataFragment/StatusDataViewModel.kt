package com.app.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDataViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
) : ViewModel() {

    private var _gralStatus = MutableLiveData(StatusRobot.INIT)
    val gralStatus: LiveData<StatusRobot> = _gralStatus

    private var _motorControllerTemp = MutableLiveData<Float?>(null)
    val motorControllerTemp: LiveData<Float?> = _motorControllerTemp

    private var _mainboardTemp = MutableLiveData<Float?>(null)
    val mainboardTemp: LiveData<Float?> = _mainboardTemp

    private var _imuTemp = MutableLiveData<Float?>(null)
    val imuTemp: LiveData<Float?> = _imuTemp

    private var _batteryStatus = MutableLiveData(Battery(false,0, 0F))
    val batteryStatus: LiveData<Battery> = _batteryStatus

    private var _statusConnection = MutableLiveData<StatusConnection>()
    val statusConnection: LiveData<StatusConnection> = _statusConnection

    private var _localIp = MutableLiveData<String>()
    val localIp: LiveData<String> = _localIp

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _mainboardTemp.postValue(it.tempMainboard)
                _imuTemp.postValue(it.tempImu)
                _motorControllerTemp.postValue(it.tempMcb)
                _gralStatus.postValue(it.statusCode)

            }
        }

        ioScope.launch {
            commsRepository.connectionState.collect {
                it.ip?.let { ip ->
                    _localIp.postValue(ip)
                }
                _statusConnection.postValue(it.status)
            }
        }
    }
}