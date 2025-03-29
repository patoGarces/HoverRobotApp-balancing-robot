package com.app.hoverrobot.ui.analisisFragment

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.comms.FrameRobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private val _newDataAnalisis = MutableStateFlow<List<FrameRobotDynamicData>>(emptyList())
    val newDataAnalisis : StateFlow<List<FrameRobotDynamicData>> get() = _newDataAnalisis

    private var _newRobotConfig : MutableLiveData<RobotLocalConfig> = MutableLiveData()
    val newRobotConfig : LiveData<RobotLocalConfig> get() = _newRobotConfig

    private var _statusCode = mutableStateOf<StatusRobot?>(null)
    val statusCode : State<StatusRobot?> get() = _statusCode

    private val initTimeStamp: Long = System.currentTimeMillis()

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect { newData ->
                val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
                _newDataAnalisis.update { oldList -> oldList + FrameRobotDynamicData(newData,actualTimeInSec) }
                _statusCode.value = newData.statusCode
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

    fun getStoredData(): List<FrameRobotDynamicData> {
        return _newDataAnalisis.value
    }
}