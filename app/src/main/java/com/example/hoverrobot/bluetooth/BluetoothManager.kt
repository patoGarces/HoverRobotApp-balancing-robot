package com.example.hoverrobot.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.BluetoothInterface
import com.example.hoverrobot.data.utils.StatusEnumBT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BluetoothManager(private val context: Context, private val interfaceBT: BluetoothInterface) {

    private var bluetoothAdapter: BluetoothAdapter = getDefaultAdapter()
    private var discoverDevicesBT = arrayListOf<BluetoothDevice>()

    private lateinit var bluetoothSocket: BluetoothSocket

    private lateinit var outputStreamBt: OutputStream
    private lateinit var inputStreamBt: InputStream
    private val TAG = "bluetoothManager"

    init {
        interfaceBT.setStatusBT(StatusEnumBT.STATUS_INIT)
    }

    fun isBluetoothEnabled(): StatusBtEnable {
        return if (bluetoothAdapter.isEnabled) {
            StatusBtEnable.BLUETOOTH_ON
        } else {
            StatusBtEnable.BLUETOOTH_OFF
        }
    }

    fun startDiscoverBT() {

        discoverDevicesBT.clear()
        loadPairedDevices()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.startDiscovery()
        interfaceBT.setStatusBT(StatusEnumBT.STATUS_DISCOVERING)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        (context as Activity).registerReceiver(receiver, filter)

        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        (context as Activity).registerReceiver(receiver, filter2)

        val filter3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        (context as Activity).registerReceiver(receiver, filter3)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        addDeviceAvailable(device)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    interfaceBT.stopDiscover()
                    if (interfaceBT.getStatusBT() != StatusEnumBT.STATUS_CONNECTING) {
                        interfaceBT.setStatusBT(StatusEnumBT.STATUS_DISCONNECT)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    interfaceBT.initDiscover()
                }
            }
        }
    }

    private fun loadPairedDevices() {
        if (bluetoothAdapter.isEnabled) {
            val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            pairedDevice?.forEach { device ->

                addDeviceAvailable(device)
            }
        }
    }

    private fun addDeviceAvailable(device: BluetoothDevice) {
        if (!discoverDevicesBT.contains(device)) {
            discoverDevicesBT.add(device)

            Log.d(TAG, "Nuevo dispositivo detectado: $discoverDevicesBT")
        }
        interfaceBT.getDevicesBT(discoverDevicesBT)
    }

    fun connectDevice(device: BluetoothDevice) {

        Log.d(TAG, "Intentando conectar con ${device.name}")
        if (StatusEnumBT.STATUS_CONNECTING == interfaceBT.getStatusBT()) {
            Log.d(TAG, "ABORTADO: ESTAS INTENTANDO CONECTARTE DURANTE UN INTENTO DE CONEXION")
            return
        }
        bluetoothAdapter.cancelDiscovery()
        interfaceBT.setStatusBT(StatusEnumBT.STATUS_CONNECTING)

        GlobalScope.launch {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothSocket.connect()
                interfaceBT.setStatusBT(StatusEnumBT.STATUS_CONNECTED)
                Log.d("connectResponse", "conexion exitosa")
                initStreams()
            } catch (e: IOException) {
                interfaceBT.setStatusBT(StatusEnumBT.STATUS_DISCONNECT)
                Log.d(TAG, "Error en intento de conexion")
            }
        }
    }

    private fun initStreams() {

        try {
            outputStreamBt = bluetoothSocket.outputStream
            inputStreamBt = bluetoothSocket.inputStream
            receiverListener()
        } catch (e: IOException) {
            interfaceBT.setStatusBT(StatusEnumBT.STATUS_DISCONNECT)
            Log.e(TAG, "Error al obtener el outputStream", e)
        }
    }

    fun sendJoystickUpdate(axisControl: AxisControl) {
        val paramList =
            listOf(HEADER_PACKET, HEADER_TX_KEY_CONTROL, axisControl.axisX, axisControl.axisY)
        val buffer =
            ByteBuffer.allocate((paramList.size + 1) * 4)                                        // Float ocupa 4 bytes, int igual, agrego 1 para el checksum

        buffer.order(ByteOrder.LITTLE_ENDIAN)

        for (intValue in paramList) {
            buffer.putInt(intValue)
        }

        val checksum =
            HEADER_PACKET xor HEADER_TX_KEY_CONTROL xor axisControl.axisX xor axisControl.axisY
        buffer.putInt(checksum)

        outputStreamBt.write(buffer.array())
    }

    fun sendPidParam(pidSettings: PidSettings) {

        val paramList = listOf(
            HEADER_PACKET,
            HEADER_TX_KEY_SETTINGS,
            (pidSettings.kp * 100).toInt(),
            (pidSettings.ki * 100).toInt(),
            (pidSettings.kd * 100).toInt(),
            (pidSettings.centerAngle * 100).toInt(),
            (pidSettings.safetyLimits * 100).toInt()
        )
        val buffer =
            ByteBuffer.allocate((paramList.size + 1) * 4)                                        // Float ocupa 4 bytes, int igual, agrego 1 para el checksum

        buffer.order(ByteOrder.LITTLE_ENDIAN)

        for (intValue in paramList) {
            buffer.putInt(intValue)
        }

        val checksum = (
                HEADER_PACKET xor
                        HEADER_TX_KEY_SETTINGS xor
                        (pidSettings.kp * 100).toInt() xor
                        (pidSettings.ki * 100).toInt() xor
                        (pidSettings.kd * 100).toInt() xor
                        (pidSettings.centerAngle * 100).toInt() xor
                        (pidSettings.safetyLimits * 100).toInt()
                )

        buffer.putInt(checksum)

        outputStreamBt.write(buffer.array())
    }

    private fun receiverListener() {

        try {
            val bufferSize = 100
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int

            do {
                bytesRead = inputStreamBt.read(buffer)
                if (bytesRead > 0) {
                    val byteBuffer = ByteBuffer.wrap(buffer)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

                    interfaceBT.newMessageReceive(byteBuffer)
                }
            } while (bytesRead >= 0)
        } catch (e: IOException) {
            interfaceBT.setStatusBT(StatusEnumBT.STATUS_DISCONNECT)
            Log.e(TAG, "Error inputStream", e)
        }
    }

    fun destroy() {
        try {
            bluetoothSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error cerrando el socket bluetooth", e)
        }
    }

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        const val HEADER_PACKET = 0xABC0
        const val HEADER_RX_KEY_STATUS =
            0xAB01           // key que indica que el paquete recibido es un status
        const val HEADER_TX_KEY_CONTROL =
            0xAB02          // key que indica qe el paquete a enviar es de control
        const val HEADER_TX_KEY_SETTINGS =
            0xAB03         // key que indica qe el paquete a enviar es de configuracion

    }
}

enum class StatusBtEnable {
    BLUETOOTH_ON,
    BLUETOOTH_OFF,
}