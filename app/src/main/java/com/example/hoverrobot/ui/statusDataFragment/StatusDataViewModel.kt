package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
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

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    init{
        _gralStatus.postValue(StatusEnumGral.STATUS_UNKNOWN.ordinal)
        _escsTemp.postValue(0F)
        _imuTemp.postValue(0F)

        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _imuTemp.postValue(it.tempUcMain.toFloat() / 10)
                _escsTemp.postValue(it.tempUcControl.toFloat() / 10)
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