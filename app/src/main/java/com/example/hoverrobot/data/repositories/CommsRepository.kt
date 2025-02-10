package com.example.hoverrobot.data.repositories

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.ConnectionState
import com.example.hoverrobot.data.models.comms.DirectionControl
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.ROBOT_DYNAMIC_DATA_SIZE
import com.example.hoverrobot.data.models.comms.ROBOT_LOCAL_CONFIG_SIZE
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.asRobotDynamicData
import com.example.hoverrobot.data.models.comms.asRobotLocalConfig
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.ToolBox.ioScope
import com.example.hoverrobot.data.utils.ToolBox.toIpString
import com.example.hoverrobot.data.utils.toByteBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

interface CommsRepository {

    val connectionState: StateFlow<ConnectionState>

    val dynamicDataRobotFlow: SharedFlow<RobotDynamicData>

    val robotLocalConfigFlow: SharedFlow<RobotLocalConfig?>

    fun sendPidParams(pidParams: PidSettings)

    fun sendDirectionControl(directionControl: DirectionControl)

    fun sendCommand(commandCode: CommandsRobot, value: Float = 0f)

    fun getConnectedClient(): String?
}

class CommsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CommsRepository {

    private val serverSocket = ServerTcp()

    private val _connectionState = MutableStateFlow(ConnectionState())                // Es un stateFlow, porque busco que emita SOLO si cambia el valor de su contenido
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _dynamicDataRobotFlow = MutableSharedFlow<RobotDynamicData>()
    override val dynamicDataRobotFlow: SharedFlow<RobotDynamicData> = _dynamicDataRobotFlow

    private val _robotLocalConfigFlow = MutableStateFlow<RobotLocalConfig?>(null)
    override val robotLocalConfigFlow: StateFlow<RobotLocalConfig?> = _robotLocalConfigFlow

    private val TAG = "CommsRepository"

    private var contPackets: Int = 0

    init {
        val bufferSize = 2048
        val byteBuffer = ByteBuffer.allocate(bufferSize)

        ioScope.launch {
            serverSocket.receivedDataFlow.collect { newPaquet ->

                byteBuffer.put(newPaquet)
                byteBuffer.flip()

                while (byteBuffer.remaining() >= 20) {

                    val headerBytes = ByteArray(2)
                    byteBuffer.get(headerBytes)

                    val byte0 = headerBytes[0].toInt() and 0xFF
                    val byte1 = headerBytes[1].toInt() and 0xFF
                    val headerPackage = ((byte1 shl 8) or byte0)        // shl es analogo a '<<' en C

                    when (headerPackage) {
                        HEADER_PACKAGE_STATUS -> {
                            try {
                                val dynamicData = ByteArray(ROBOT_DYNAMIC_DATA_SIZE)
                                byteBuffer.get(dynamicData)
                                contPackets++
                                _dynamicDataRobotFlow.emit(dynamicData.toByteBuffer().asRobotDynamicData)
                            }
                            catch (e: BufferUnderflowException) {
                                Log.e(TAG,"error: $e")
                            }
                        }

                        HEADER_PACKAGE_LOCAL_CONFIG -> {
                            val localConfig = ByteArray(ROBOT_LOCAL_CONFIG_SIZE)
                            byteBuffer.get(localConfig)
                            contPackets++
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
            while(true) {
                val wifiInfo = getWifiInfo(context)
                _connectionState.emit(
                    ConnectionState(
                        status = serverSocket.connectionsStatus.value,
                        receiverPacketRates = contPackets,
                        rssi = wifiInfo.rssi,
                        strength = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5),
                        frequency = wifiInfo.frequency,
                        ip = wifiInfo.ipAddress.toIpString()
                    )
                )
                contPackets = 0
                delay(1000)
            }
        }

        ioScope.launch {
            serverSocket.connectionsStatus.collect {
                if (it == StatusConnection.WAITING) {
                    _robotLocalConfigFlow.emit(null)                                            // Para forzar el collect al reconectar
                }
            }
        }
    }

    override fun sendPidParams(pidParams: PidSettings) {
        val paramList = listOf(
            HEADER_PACKAGE_SETTINGS,
            pidParams.indexPid.toShort(),
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

    override fun sendDirectionControl(directionControl: DirectionControl) {
        val paramList =
            listOf(HEADER_PACKAGE_CONTROL.toShort(), directionControl.joyAxisX, directionControl.joyAxisY)
        val buffer = ByteBuffer.allocate(paramList.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        serverSocket.sendData(buffer.array())
    }

    override fun sendCommand(commandCode: CommandsRobot, value: Float) {

        val valueCommand = value * PRECISION_DECIMALS_COMMS
        val paramList =
            listOf(
                HEADER_PACKAGE_COMMAND.toShort(),
                commandCode.ordinal.toShort(),
                valueCommand.toInt().toShort()
            )
        val buffer = ByteBuffer.allocate(paramList.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        serverSocket.sendData(buffer.array())
    }
    override fun getConnectedClient(): String? {
        return serverSocket.clientsIp
    }

    private fun getWifiInfo(context: Context): WifiInfo {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WifiInfo.Builder().build()
        } else {
            wifiManager.connectionInfo // Para API < 30
        }
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