package com.example.hoverrobot.data.repositories

import android.content.Context
import android.util.Log
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.ROBOT_DYNAMIC_DATA_SIZE
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.asRobotDynamicData
import com.example.hoverrobot.data.models.comms.asRobotLocalConfig
import com.example.hoverrobot.data.utils.ByteArraysUtils.toByteBuffer
import com.example.hoverrobot.data.utils.ConnectionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

interface CommsRepository {

    val dynamicDataRobotFlow: SharedFlow<RobotDynamicData>

    val robotLocalConfigFlow: SharedFlow<RobotLocalConfig>

    val connectionStateFlow: StateFlow<ConnectionStatus>

    fun sendPidParams(pidParams: PidSettings)

    fun sendJoystickUpdate(axisControl: AxisControl)

    fun sendCommand(commandCode: Short)

    fun getConnectedClients(): List<String>?
}

class CommsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CommsRepository {


    private val serverSocket = ServerTcp()

    private val _dynamicDataRobotFlow = MutableSharedFlow<RobotDynamicData>()
    override val dynamicDataRobotFlow: SharedFlow<RobotDynamicData> = _dynamicDataRobotFlow

    private val _robotLocalConfigFlow = MutableStateFlow<RobotLocalConfig>(RobotLocalConfig(0f,0f,0f,0f,0f))
    override val robotLocalConfigFlow: StateFlow<RobotLocalConfig> = _robotLocalConfigFlow



    private val _connectionStateFlow = MutableStateFlow(ConnectionStatus.INIT)
    override val connectionStateFlow: StateFlow<ConnectionStatus> = _connectionStateFlow

    private val TAG = "CommsRepository"

    init {
        commsHandler()
    }

    private var contador = 0
    private fun commsHandler() {
        val bufferSize = 2048
        val byteBuffer = ByteBuffer.allocate(bufferSize)

        ioScope.launch {
            serverSocket.receivedDataFlow.collect { newPaquet ->

                byteBuffer.put(newPaquet)
                byteBuffer.flip()

                while (byteBuffer.remaining() >= 20) {

                    val headerBytes = ByteArray(2)
                    byteBuffer.get(headerBytes)

                    val byte0 = headerBytes.get(0).toInt() and 0xFF
                    val byte1 = headerBytes.get(1).toInt() and 0xFF
                    val headerPackage = ((byte1 shl 8) or byte0)        // shl es analogo a '<<' en C

                    when (headerPackage) {
                        HEADER_PACKAGE_STATUS -> {
                            val dynamicData = ByteArray(ROBOT_DYNAMIC_DATA_SIZE)
                            byteBuffer.get(dynamicData)
                            _dynamicDataRobotFlow.emit(dynamicData.toByteBuffer().asRobotDynamicData)
//                            contador++
//                            Log.d(TAG,"paquetes recibidos: $contador")
                        }

                        HEADER_PACKAGE_LOCAL_CONFIG -> {
                            val localConfig = ByteArray(10)                 // TODO: Eliminar hardcode
                            byteBuffer.get(localConfig)
                            _robotLocalConfigFlow.emit(localConfig.toByteBuffer().asRobotLocalConfig)
                        }

                        else -> {
                            Log.d(TAG, "Unrecognized package: $headerPackage")
                        }
                    }
                }
                byteBuffer.compact()
            }
        }

        ioScope.launch {
            serverSocket.connectionsStatus.collect {
                _connectionStateFlow.emit(it)
            }
        }
    }

    override fun sendPidParams(pidParams: PidSettings) {
        val paramList = listOf(
            HEADER_PACKAGE_SETTINGS,
            (pidParams.kp * PRECISION_DECIMALS_COMMS).toInt().toShort(),
            (pidParams.ki * PRECISION_DECIMALS_COMMS).toInt().toShort(),
            (pidParams.kd * PRECISION_DECIMALS_COMMS).toInt().toShort(),
            (pidParams.centerAngle * PRECISION_DECIMALS_COMMS).toInt().toShort(),
            (pidParams.safetyLimits * PRECISION_DECIMALS_COMMS).toInt().toShort()
        )
        val buffer =
            ByteBuffer.allocate(paramList.size * 2) // Float ocupa 4 bytes, int igual, short 2
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it.toShort()) }

        serverSocket.sendData(buffer.array())
    }

    override fun sendJoystickUpdate(axisControl: AxisControl) {
        val paramList =
            listOf(HEADER_PACKAGE_CONTROL.toShort(), axisControl.axisX, axisControl.axisY)
        val buffer = ByteBuffer.allocate(paramList.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        serverSocket.sendData(buffer.array())
    }

    override fun sendCommand(commandCode: Short) {
        val paramList =
            listOf(HEADER_PACKAGE_COMMAND.toShort(), commandCode)
        val buffer = ByteBuffer.allocate(paramList.size * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        serverSocket.sendData(buffer.array())
    }
    override fun getConnectedClients(): List<String>? {
        return serverSocket.clientsIp
    }

    companion object {
        const val HEADER_PACKAGE_STATUS: Int = 0xAB01       // Package recepcion
        const val HEADER_PACKAGE_CONTROL: Int = 0xAB02      // Package transmision
        const val HEADER_PACKAGE_SETTINGS: Int = 0xAB03     // Package transmision
        const val HEADER_PACKAGE_COMMAND: Int = 0xAB04      // Package transmision
        const val HEADER_PACKAGE_LOCAL_CONFIG: Int = 0xAB05 // Package recepcion
    }
}

const val PRECISION_DECIMALS_COMMS = 100    // Precision al convertir a float, en este caso 100 = 0.01