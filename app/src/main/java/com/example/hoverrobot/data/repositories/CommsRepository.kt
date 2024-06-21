package com.example.hoverrobot.data.repositories

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.example.hoverrobot.bluetooth.BLEManager
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.asRobotDynamicData
import com.example.hoverrobot.data.models.comms.asRobotLocalConfig
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

    val availableDevices: SharedFlow<List<BluetoothDevice>>

    val connectionStateFlow: StateFlow<ConnectionStatus>

    fun sendPidParams(pidParams: PidSettings)

    fun sendJoystickUpdate(axisControl: AxisControl)

    fun sendCommand(commandCode: Short)

    fun connectDevice(device: BluetoothDevice)

    fun getConnectedDevice(): BluetoothDevice?

    fun startDiscoverBT()

    fun isBluetoothEnabled(): Boolean
}

class CommsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CommsRepository {

    private var bleManager = BLEManager(context)

    private val _dynamicDataRobotFlow = MutableSharedFlow<RobotDynamicData>()
    override val dynamicDataRobotFlow: SharedFlow<RobotDynamicData> = _dynamicDataRobotFlow

    private val _robotLocalConfigFlow = MutableSharedFlow<RobotLocalConfig>()
    override val robotLocalConfigFlow: SharedFlow<RobotLocalConfig> = _robotLocalConfigFlow

    private val _availableDevices = MutableSharedFlow<List<BluetoothDevice>>()
    override val availableDevices: SharedFlow<List<BluetoothDevice>> = _availableDevices

    private val _connectionStateFlow = MutableStateFlow(ConnectionStatus.INIT)
    override val connectionStateFlow: StateFlow<ConnectionStatus> = _connectionStateFlow

    private val TAG = "CommsRepository"

    init {
        setupObservers()
    }

    private fun setupObservers() {
        ioScope.launch {
            bleManager.receivedDataBtFlow.collect {
//                Log.d(TAG,"SIZE COLLECT: ${ it.remaining()}")
//                if(it.remaining() > 20) {

                val byte0 = it.get(0).toInt() and 0xFF
                val byte1 = it.get(1).toInt() and 0xFF
                val headerPackage =
                    ((byte1 shl 8) or byte0)              // shl es analogo a '<<' en C, es para desplazar bits a la izquierda

                when (headerPackage) {
                    HEADER_PACKAGE_STATUS -> {
                        _dynamicDataRobotFlow.emit(it.asRobotDynamicData)
                    }

                    HEADER_PACKAGE_LOCAL_CONFIG -> {
                        Log.d(TAG,"NewConfigs received")
                        _robotLocalConfigFlow.emit(it.asRobotLocalConfig)
                    }

                    else -> Log.d(TAG, "Unrecognized package: $headerPackage")
                }
            }
        }

        ioScope.launch {
            bleManager.connectionsStatus.collect {
                _connectionStateFlow.emit(it)
            }
        }

        ioScope.launch {
            bleManager.availableBtDevices.collect {
                ioScope.launch { _availableDevices.emit(it) }
            }
        }
    }

    override fun sendPidParams(pidParams: PidSettings) {
        val paramList = listOf(
            HEADER_PACKAGE_SETTINGS,
            (pidParams.kp * 100).toInt().toShort(),
            (pidParams.ki * 100).toInt().toShort(),
            (pidParams.kd * 100).toInt().toShort(),
            (pidParams.centerAngle * 100).toInt().toShort(),
            (pidParams.safetyLimits * 100).toInt().toShort()
        )
        val buffer =
            ByteBuffer.allocate(paramList.size * 2) // Float ocupa 4 bytes, int igual, short 2
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it.toShort()) }

        bleManager.sendData(buffer.array())
    }

    override fun sendJoystickUpdate(axisControl: AxisControl) {
        val paramList =
            listOf(HEADER_PACKAGE_CONTROL.toShort(), axisControl.axisX, axisControl.axisY)
        val buffer = ByteBuffer.allocate(paramList.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }
        bleManager.sendData(buffer.array())
    }

    override fun sendCommand(commandCode: Short) {
        val paramList =
            listOf(HEADER_PACKAGE_COMMAND.toShort(), commandCode)
        val buffer = ByteBuffer.allocate(paramList.size * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putShort(it) }

        bleManager.sendData(buffer.array())
    }

    override fun connectDevice(device: BluetoothDevice) {
        bleManager.connectDevice(device)
    }

    override fun startDiscoverBT() {
        bleManager.startScan()
    }

    override fun isBluetoothEnabled(): Boolean {
        return bleManager.isBluetoothEnabled()
    }

    override fun getConnectedDevice(): BluetoothDevice? {
        return bleManager.getDeviceConnected()
    }

    companion object {
        const val HEADER_PACKAGE_STATUS: Int = 0xAB01       // Package recepcion
        const val HEADER_PACKAGE_CONTROL: Int = 0xAB02      // Package transmision
        const val HEADER_PACKAGE_SETTINGS: Int = 0xAB03     // Package transmision
        const val HEADER_PACKAGE_COMMAND: Int = 0xAB04      // Package transmision
        const val HEADER_PACKAGE_LOCAL_CONFIG: Int = 0xAB05 // Package recepcion
    }
}

const val PRECISION_DECIMALS_COMMS =
    100    // Precision al convertir la data cruda del BLE a float, en este caso 100 = 0.01