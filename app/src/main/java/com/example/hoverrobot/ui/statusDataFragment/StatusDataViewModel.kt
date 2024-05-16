package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.StatusEnumGral
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDataViewModel @Inject constructor(
    private val commsRepository: CommsRepository
) : ViewModel() {

    private var _gralStatus = MutableLiveData<Int>()
    val gralStatus : LiveData<Int> = _gralStatus

    private var _escsTemp = MutableLiveData<Float>()
    val escsTemp : LiveData<Float> = _escsTemp

    private var _imuTemp = MutableLiveData<Float>()
    val imuTemp : LiveData<Float> = _imuTemp

    private var _batteryTemp = MutableLiveData<Float>()
    val batteryTemp : LiveData<Float> = _batteryTemp

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    init{
        _gralStatus.postValue(StatusEnumGral.UNKNOWN.ordinal)
        _escsTemp.postValue(0F)
        _imuTemp.postValue(0F)
        _batteryTemp.postValue(0F)

        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _escsTemp.postValue(it.tempEsc.toFloat() / 10)
                _imuTemp.postValue(it.tempImu.toFloat() / 10)
                _batteryTemp.postValue(it.batTemp.toFloat() / 10)
                _gralStatus.postValue(it.statusCode.toInt())

            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _connectionStatus.postValue(it)
            }
        }
    }
}