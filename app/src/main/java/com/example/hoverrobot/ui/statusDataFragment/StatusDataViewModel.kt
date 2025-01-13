package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.Battery
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.StatusEnumGral
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDataViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
) : ViewModel() {

    private var _gralStatus = MutableLiveData<StatusEnumGral>()
    val gralStatus : LiveData<StatusEnumGral> = _gralStatus

    private var _motorControllerTemp = MutableLiveData<Float>()
    val motorControllerTemp : LiveData<Float> = _motorControllerTemp

    private var _mainboardTemp = MutableLiveData<Float>()
    val mainboardTemp : LiveData<Float> = _mainboardTemp

    private var _batteryStatus = MutableLiveData<Battery>()
    val batteryStatus : LiveData<Battery> = _batteryStatus

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    private var _localIp = MutableLiveData<String>()
    val localIp : LiveData<String> = _localIp

    init {
        _gralStatus.postValue(StatusEnumGral.DISCONNECTED)
        _motorControllerTemp.postValue(0F)
        _mainboardTemp.postValue(0F)
        _batteryStatus.postValue(Battery(0,0F))

        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
//                _escsTemp.postValue(it.tempEsc.toFloat() / 10)                                    // TODO: recibir datos de bateria y temp
                _mainboardTemp.postValue(it.tempImu)
                _gralStatus.postValue(StatusEnumGral.entries[it.statusCode])

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