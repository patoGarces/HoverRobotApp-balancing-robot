package com.example.hoverrobot.data.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.example.hoverrobot.bluetooth.BluetoothManager
import com.example.hoverrobot.bluetooth.StatusBtEnable
import com.example.hoverrobot.data.models.BluetoothInterface
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.models.comms.MainBoardRobotStatus
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.asRobotStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CommsRepository(private val context: Context, bluetoothInterface: BluetoothInterface) {   // TODO: hacer context y bluetooth manager injectable, eliminar bluetooth interface

    private var bluetoothManager: BluetoothManager

    private val _statusRobotFlow = MutableSharedFlow<MainBoardRobotStatus>()
    val statusRobotFlow: SharedFlow<MainBoardRobotStatus> = _statusRobotFlow

    private val _sendDataBtFlow = MutableSharedFlow<ByteBuffer>()        // TODO: revisar replay
    val sendDataBtFlow: SharedFlow<ByteBuffer> = _sendDataBtFlow

    private val ioScope = CoroutineScope(Dispatchers.IO)
    init {
        bluetoothManager = BluetoothManager(context,bluetoothInterface)
        setupObservers()
    }

    private fun setupObservers() {
        ioScope.launch {
            bluetoothManager.receivedDataBtFlow.collect {
                _statusRobotFlow.emit(it.asRobotStatus)                                             // El dia de mañana si se reciben otros tipos de datos, se deberia hacer el split aca
            }
        }
    }


    fun sendPidParam(pidParams: PidSettings) {
        val paramList = listOf(
            HEADER_PACKET,
            HEADER_TX_KEY_SETTINGS,
            (pidParams.kp * 100).toInt(),
            (pidParams.ki * 100).toInt(),
            (pidParams.kd * 100).toInt(),
            (pidParams.centerAngle * 100).toInt(),
            (pidParams.safetyLimits * 100).toInt()
        )
        val buffer = ByteBuffer.allocate((paramList.size + 1) * 4) // Float ocupa 4 bytes, int igual, short 2, agrego 1 para el checksum

        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putInt(it) }

        val checksum = (
            HEADER_PACKET xor
            HEADER_TX_KEY_SETTINGS xor
            (pidParams.kp * 100).toInt() xor
            (pidParams.ki * 100).toInt() xor
            (pidParams.kd * 100).toInt() xor
            (pidParams.centerAngle * 100).toInt() xor
            (pidParams.safetyLimits * 100).toInt()
        )

        buffer.putInt(checksum)
        bluetoothManager.sendDataBt(buffer)
    }

    fun sendJoystickUpdate(axisControl: AxisControl) {
        val paramList =
            listOf(HEADER_PACKET, HEADER_TX_KEY_CONTROL, axisControl.axisX, axisControl.axisY)
        val buffer = ByteBuffer.allocate((paramList.size + 1) * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        paramList.forEach { buffer.putInt(it) }

        val checksum =
            HEADER_PACKET xor HEADER_TX_KEY_CONTROL xor axisControl.axisX xor axisControl.axisY
        buffer.putInt(checksum)
        bluetoothManager.sendDataBt(buffer)
    }

    fun connectDevice(device: BluetoothDevice) {        // TODO: borrar
        bluetoothManager.connectDevice(device)
    }

    fun startDiscoverBT() {
        bluetoothManager.startDiscoverBT()
    }

    fun isBluetoothEnabled(): StatusBtEnable {
        return bluetoothManager.isBluetoothEnabled()
    }
    companion object {
        const val HEADER_PACKET = 0xABC0
        const val HEADER_RX_KEY_STATUS = 0xAB01  // key que indica que el paquete recibido es un status
        const val HEADER_TX_KEY_CONTROL = 0xAB02  // key que indica qe el paquete a enviar es de control
        const val HEADER_TX_KEY_SETTINGS = 0xAB03  // key que indica qe el paquete a enviar es de configuracion
    }
}