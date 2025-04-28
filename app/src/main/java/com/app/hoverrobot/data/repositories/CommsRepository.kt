package com.app.hoverrobot.data.repositories

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.app.hoverrobot.data.models.comms.CommandsRobot
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.DirectionControl
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.ROBOT_DYNAMIC_DATA_SIZE
import com.app.hoverrobot.data.models.comms.ROBOT_LOCAL_CONFIG_SIZE
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.comms.asRobotDynamicData
import com.app.hoverrobot.data.models.comms.asRobotLocalConfig
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.data.utils.ToolBox.toIpString
import com.app.hoverrobot.data.utils.toByteBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject


val IP_ADDRESS_CLIENT_RASPI_DEFAULT = "192.168.0.102"
val IP_ADDRESS_CLIENT_ROBOT_DEFAULT = "192.168.0.101"
val APP_DEFAULT_PORT = 8080

interface CommsRepository {
    val connectionNetworkState: StateFlow<NetworkState>

    val dynamicDataRobotFlow: SharedFlow<RobotDynamicData>

    val robotLocalConfigFlow: SharedFlow<RobotLocalConfig?>

    val localIp: StateFlow<String?>

    fun reconnectRobotSocket(serverIp: String, port: Int)

    fun reconnectRaspiSocket(serverIp: String, port: Int)

    fun sendPidParams(pidParams: PidSettings)

    fun sendDirectionControl(directionControl: DirectionControl)

    fun sendCommand(commandCode: CommandsRobot, value: Float = 0f)
}

class CommsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CommsRepository {

//    private val socketTCP = ServerTcpImpl()
    private val socketClientRobot = ClientTcpImpl()
    private val socketClientRaspi = ClientTcpImpl()

    private val _connectionNetworkState = MutableStateFlow(NetworkState())                      // Es un stateFlow, porque busco que emita SOLO si cambia el valor de su contenido
    override val connectionNetworkState: StateFlow<NetworkState> = _connectionNetworkState

    private val _localIp = MutableStateFlow<String?>(null)                // Es un stateFlow, porque busco que emita SOLO si cambia el valor de su contenido
    override val localIp: StateFlow<String?> = _localIp

    private val _dynamicDataRobotFlow = MutableSharedFlow<RobotDynamicData>(
        replay = 0,
        extraBufferCapacity = 1000                                      // Este buffer me permite que si demora el collect, guardar los datos hast que se puedan procesar
    )
    override val dynamicDataRobotFlow: SharedFlow<RobotDynamicData> = _dynamicDataRobotFlow

    private val _robotLocalConfigFlow = MutableStateFlow<RobotLocalConfig>(RobotLocalConfig())
    override val robotLocalConfigFlow: StateFlow<RobotLocalConfig> = _robotLocalConfigFlow

    private val TAG = "CommsRepository"

    private var contPackets: Int = 0

    init {
        val bufferSize = 2048
        val byteBuffer = ByteBuffer.allocate(bufferSize)

        ioScope.launch {
            socketClientRobot.receivedDataFlow.collect { newPacket ->
                byteBuffer.put(newPacket)
                byteBuffer.flip()

                while (true) {
                    if (byteBuffer.remaining() < 2) {
                        // No hay suficientes bytes para leer un header
                        break
                    }

                    byteBuffer.mark() // Marca la posición antes de leer
                    val headerBytes = ByteArray(2)
                    byteBuffer.get(headerBytes)

                    val byte0 = headerBytes[0].toInt() and 0xFF
                    val byte1 = headerBytes[1].toInt() and 0xFF
                    val headerPackage = ((byte1 shl 8) or byte0)

                    val expectedSize = when (headerPackage) {
                        HEADER_PACKAGE_STATUS -> ROBOT_DYNAMIC_DATA_SIZE
                        HEADER_PACKAGE_LOCAL_CONFIG -> ROBOT_LOCAL_CONFIG_SIZE
                        else -> {
                            Log.i(TAG, "Unrecognized package: 0x${headerPackage.toString(16)}")
                            continue
                        }
                    }

                    if (byteBuffer.remaining() < expectedSize) {
                        // No tenemos todos los datos todavía, esperamos a la próxima recepción
                        byteBuffer.reset() // Volvemos al punto anterior (antes de leer el header)
                        break
                    }

                    val dataBytes = ByteArray(expectedSize)
                    byteBuffer.get(dataBytes)

                    when (headerPackage) {
                        HEADER_PACKAGE_STATUS -> {
                            contPackets++
                            _dynamicDataRobotFlow.emit(dataBytes.toByteBuffer().asRobotDynamicData)
                        }
                        HEADER_PACKAGE_LOCAL_CONFIG -> _robotLocalConfigFlow.emit(dataBytes.toByteBuffer().asRobotLocalConfig)
                        else -> Log.i(TAG, "Unrecognized len package: 0x${headerPackage.toString(16)} , size: ${dataBytes.size}")
                    }
                }

                byteBuffer.compact()
            }
        }

        ioScope.launch {
            while(true) {
                val wifiInfo = getWifiInfo(context)
                _localIp.emit(wifiInfo.ipAddress.toIpString())

                _connectionNetworkState.emit(
                    NetworkState(
                        status = StatusConnection.INIT,                             // TODO: monitorizar la conexion wifi
                        statusRobotClient = ConnectionState(
                            status = socketClientRobot.connectionsStatus.value,
                            receiverPacketRates = contPackets,
                            addressIp = socketClientRobot.serverAddress,
                        ),
                        statusRaspiClient = ConnectionState(
                            status = socketClientRaspi.connectionsStatus.value,
//                            receiverPacketRates = 0,
                            addressIp = socketClientRaspi.serverAddress,
                        ),
                        rssi = wifiInfo.rssi,
                        strength = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5),
                        frequency = wifiInfo.frequency,
                        localIp = wifiInfo.ipAddress.toIpString(),
                    )
                )

                contPackets = 0
                delay(1000)
            }
        }

        ioScope.launch {
            socketClientRobot.connectionsStatus.collect {
                if (it == StatusConnection.SEARCHING) {
                    _robotLocalConfigFlow.emit(RobotLocalConfig())                                            // Para forzar el collect al reconectar
                }
            }
        }
    }

    override fun reconnectRobotSocket(serverIp: String, port: Int) {
        socketClientRobot.reconnect(serverIp, port)
    }

    override fun reconnectRaspiSocket(serverIp: String, port: Int) {
        socketClientRaspi.reconnect(serverIp, port)
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

        socketClientRobot.sendData(buffer.array())
    }

    override fun sendDirectionControl(directionControl: DirectionControl) {
        val paramList =
            listOf(HEADER_PACKAGE_CONTROL.toShort(), directionControl.joyAxisX, directionControl.joyAxisY)
        val buffer = ByteBuffer.allocate(paramList.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        socketClientRobot.sendData(buffer.array())
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

        socketClientRobot.sendData(buffer.array())
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