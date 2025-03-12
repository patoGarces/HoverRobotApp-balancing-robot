package com.app.hoverrobot.ui.analisisFragment

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _newDataAnalisis : MutableLiveData<RobotDynamicData> = MutableLiveData()
    val newDataAnalisis : LiveData<RobotDynamicData> get() = _newDataAnalisis

    private var _newRobotConfig : MutableLiveData<RobotLocalConfig> = MutableLiveData()
    val newRobotConfig : LiveData<RobotLocalConfig> get() = _newRobotConfig

    private var _statusCode = mutableStateOf<StatusRobot?>(null)
    val statusCode : State<StatusRobot?> get() = _statusCode

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _newDataAnalisis.postValue(it)
                _statusCode.value = it.statusCode
            }
        }

        ioScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it?.let {
                    _newRobotConfig.postValue(it)
                }
            }
        }
    }
}