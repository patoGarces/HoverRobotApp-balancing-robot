package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.Battery
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

    private var _batteryStatus = MutableLiveData<Battery>()
    val batteryStatus : LiveData<Battery> = _batteryStatus

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    private var _localIp = MutableLiveData<String>()
    val localIp : LiveData<String> = _localIp

    init{
        _gralStatus.postValue(StatusEnumGral.UNKNOWN.ordinal)
        _escsTemp.postValue(0F)
        _imuTemp.postValue(0F)
        _batteryStatus.postValue(Battery(0,0F))

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
//                _escsTemp.postValue(it.tempEsc.toFloat() / 10)                                    // TODO: recibir datos de bateria y temp
                _imuTemp.postValue(it.tempImu)
                _gralStatus.postValue(it.statusCode)

            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _connectionStatus.postValue(it)

                _localIp.postValue(commsRepository.getLocalIp())    // TODO: mover de aca, solo provisorio.
            }
        }
    }
}