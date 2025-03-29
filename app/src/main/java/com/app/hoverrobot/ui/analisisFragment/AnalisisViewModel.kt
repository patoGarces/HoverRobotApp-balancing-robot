package com.app.hoverrobot.ui.analisisFragment

import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _listOfDynamicData = MutableStateFlow(mutableListOf<RobotDynamicData>())
    val listOfDynamicData: StateFlow<List<RobotDynamicData>> = _listOfDynamicData

    init {
//        ioScope.launch {
//            commsRepository.dynamicDataRobotFlow.collect { dynamicData ->
//                _newDataAnalisis.postValue(dynamicData)
//                _statusCode.value = dynamicData.statusCode
//
//                _listOfDynamicData.value.add(dynamicData)           // Agrega sin copiar
//                _listOfDynamicData.value = _listOfDynamicData.value // Notifica Compose
//            }
//        }

        ioScope.launch {
            while (true) {
                val randomData = RobotDynamicData(
                    batVoltage = (Math.random() * 100).toFloat(),
                    tempImu = (Math.random() * 100).toFloat(),
                    tempMcb = (Math.random() * 100).toFloat(),
                    tempMainboard = (Math.random() * 100).toFloat(),
                    speedR = Math.random().toInt(),
                    speedL = Math.random().toInt(),
                    pitchAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    rollAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    yawAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    posInMeters = ((Math.random() - 0.5) * 180).toFloat(),
                    outputYawControl = ((Math.random() - 0.5) * 180).toFloat(),
                    setPointAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    setPointPos = ((Math.random() - 0.5) * 180).toFloat(),
                    setPointYaw = ((Math.random() - 0.5) * 180).toFloat(),
                    setPointSpeed = ((Math.random() - 0.5) * 180).toFloat(),
                    centerAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    statusCode = StatusRobot.STABILIZED,
                    isCharging = false,
                    currentL = 0F,
                    currentR = 0F
                )

                _newDataAnalisis.postValue(randomData)
                _statusCode.value = randomData.statusCode

//                _listOfDynamicData.value.add(randomData)           // Agrega sin copiar
//                _listOfDynamicData.value = _listOfDynamicData.value // Notifica Compose
                _listOfDynamicData.value = _listOfDynamicData.value.toMutableList().apply { add(randomData) }


                delay(25)
                Log.i("AnalisisViewModel", "NewPoint")
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

    fun clearDynamicData() {
        _listOfDynamicData.value.clear()
    }
}