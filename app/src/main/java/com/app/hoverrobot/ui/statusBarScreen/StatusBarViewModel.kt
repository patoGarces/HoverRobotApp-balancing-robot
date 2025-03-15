package com.app.hoverrobot.ui.statusBarScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.toPercentLevel
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusRobot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf

@HiltViewModel
open class StatusBarViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    var battery by mutableStateOf(Battery())

    var tempImu by mutableFloatStateOf(0F)

    var statusRobot by mutableStateOf(StatusRobot.INIT)

    var connectionState by mutableStateOf(ConnectionState())

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                battery = Battery(
                        it.isCharging,
                        it.batVoltage.toPercentLevel(),
                        it.batVoltage
                    )

                tempImu = it.tempImu
                statusRobot = it.statusCode
            }
        }


        ioScope.launch {
            commsRepository.connectionState.collect {
                connectionState = it
            }
        }
    }
}