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
import com.example.hoverrobot.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.utils.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BluetoothManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter = getDefaultAdapter()
    private var discoverDevicesBT = arrayListOf<BluetoothDevice>()

    private lateinit var bluetoothSocket: BluetoothSocket

    private lateinit var outputStreamBt: OutputStream
    private lateinit var inputStreamBt: InputStream
    private val TAG = "bluetoothManager"

    private val _receivedDataBtFlow = MutableSharedFlow<ByteBuffer>()
    val receivedDataBtFlow: SharedFlow<ByteBuffer> = _receivedDataBtFlow

    private val _connectionsStatus = MutableSharedFlow<ConnectionStatus>()
    val connectionsStatus: SharedFlow<ConnectionStatus> = _connectionsStatus

    private val _availableBtDevices = MutableSharedFlow<List<BluetoothDevice>>()
    val availableBtDevices: SharedFlow<List<BluetoothDevice>> = _availableBtDevices

    private var actualConnectionStatus: ConnectionStatus = ConnectionStatus.INIT

    init {
        setConnectionStatus(ConnectionStatus.INIT)
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun startDiscoverBT() {

        discoverDevicesBT.clear()
        loadPairedDevices()

        bluetoothAdapter.startDiscovery()
        setConnectionStatus(ConnectionStatus.DISCOVERING)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        context.registerReceiver(receiver, filter2)

        val filter3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter3)
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
                    if (actualConnectionStatus != ConnectionStatus.CONNECTING) {
                        setConnectionStatus(ConnectionStatus.DISCONNECT)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    setConnectionStatus(ConnectionStatus.DISCOVERING)
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
        ioScope.launch { _availableBtDevices.emit(discoverDevicesBT) }
    }

    fun connectDevice(device: BluetoothDevice) {

        Log.d(TAG, "Intentando conectar con ${device.name}")
        if (ConnectionStatus.CONNECTING == actualConnectionStatus) {
            Log.d(TAG, "ABORTADO: ESTAS INTENTANDO CONECTARTE DURANTE UN INTENTO DE CONEXION")
            return
        }
        bluetoothAdapter.cancelDiscovery()
        setConnectionStatus(ConnectionStatus.CONNECTING)

        GlobalScope.launch {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothSocket.connect()
                setConnectionStatus(ConnectionStatus.CONNECTED)
                Log.d("connectResponse", "conexion exitosa")
                initStreams()
            } catch (e: IOException) {
                setConnectionStatus(ConnectionStatus.DISCONNECT)
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
            setConnectionStatus(ConnectionStatus.DISCONNECT)
            Log.e(TAG, "Error al obtener el outputStream", e)
        }
    }

    fun sendDataBt(buffer: ByteBuffer) {
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

                    ioScope.launch {
                        _receivedDataBtFlow.emit(byteBuffer)
                    }
                }
            } while (bytesRead >= 0)
        } catch (e: IOException) {
            setConnectionStatus(ConnectionStatus.DISCONNECT)
            Log.e(TAG, "Error inputStream", e)
        }
    }

    private fun setConnectionStatus(connectionStatus: ConnectionStatus) {
        actualConnectionStatus = connectionStatus
        ioScope.launch { _connectionsStatus.emit(connectionStatus) }
    }

    fun destroy() {
        try {
            bluetoothSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error cerrando el socket bluetooth", e)
        }
    }
}

private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
