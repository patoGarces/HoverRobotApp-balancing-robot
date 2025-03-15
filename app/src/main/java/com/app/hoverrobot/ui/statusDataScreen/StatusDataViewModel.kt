package com.app.hoverrobot.ui.statusDataScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

@HiltViewModel
class StatusDataViewModel @Inject constructor(
    private val commsRepository: CommsRepository,
) : ViewModel() {

    var gralStatus by mutableStateOf(StatusRobot.INIT)
        internal set

    var statusConnection by mutableStateOf(StatusConnection.INIT)
        internal set

    var motorControllerTemp by mutableFloatStateOf(0F)
        internal set

    var mainboardTemp by mutableFloatStateOf(0F)
        internal set

    var imuTemp by mutableFloatStateOf(0F)
        internal set

    var localIp by mutableStateOf<String?>(null)
        internal set

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                mainboardTemp = it.tempMainboard
                imuTemp = it.tempImu
                motorControllerTemp = it.tempMcb
                gralStatus = it.statusCode

            }
        }

        ioScope.launch {
            commsRepository.connectionState.collect {
                localIp = it.ip
                statusConnection = it.status
            }
        }
    }
}