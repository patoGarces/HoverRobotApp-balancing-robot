package com.example.hoverrobot.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.util.Log
import com.example.hoverrobot.data.utils.ByteArraysUtils.toByteBuffer
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.ConnectionStatus.CONNECTED
import com.example.hoverrobot.data.utils.ConnectionStatus.CONNECTING
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCOVERING
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCONNECT
import com.example.hoverrobot.data.utils.FormatPrintsLogs.toHex
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.Locale
import java.util.UUID


interface BluetoothManagerInterface {

    fun isBluetoothEnabled(): Boolean

    fun startScan()

    fun connectDevice(device: BluetoothDevice)

    suspend fun disconnectDevice()

    fun sendData(data: ByteArray)

    fun getDeviceConnected(): BluetoothDevice?
}


class BLEManager(private val context: Context): BluetoothManagerInterface {

    private val TAG = ""

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var scanning = false
    private val handler = Handler()

    private var discoveredDevices = arrayListOf<BluetoothDevice>()

    private val _receivedDataBtFlow = MutableSharedFlow<ByteBuffer>()
    val receivedDataBtFlow: SharedFlow<ByteBuffer> = _receivedDataBtFlow

    private val _connectionsStatus = MutableSharedFlow<ConnectionStatus>()
    val connectionsStatus: SharedFlow<ConnectionStatus> = _connectionsStatus

    private val _availableBtDevices = MutableSharedFlow<List<BluetoothDevice>>()
    val availableBtDevices: SharedFlow<List<BluetoothDevice>> = _availableBtDevices

    private var bluetoothGatt: BluetoothGatt? = null

    internal var actualConnectionStatus: ConnectionStatus = ConnectionStatus.INIT
    internal var deviceConnected: BluetoothDevice? = null
    internal var servicesAvailableBLE: MutableList<BluetoothGattService> = arrayListOf()

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")                     // TODO: manejar correctamente los permisos
    override fun connectDevice(device: BluetoothDevice) {
        changeStatus(CONNECTING)

        val gattCallback = object: BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)

                when(newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        changeStatus(CONNECTED)
                        bluetoothGatt?.discoverServices()
//                        bluetoothGatt?.requestMtu(MTU_SIZE)

                        bluetoothGatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    }

                    BluetoothGatt.STATE_CONNECTING -> { changeStatus(CONNECTING) }

                    else -> {
                        changeStatus(DISCONNECT)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
//                servicesAvailableBLE = gatt.services
//                gatt.services.forEach{
//                    Log.d(TAG,"uuid service: ${it.uuid}")
//                    it.characteristics.forEach {
//                        Log.d(TAG,"Caracteristicas de este servicio: servicio: ${it.service}, descriptor: ${it.descriptors}")
//                    }
//                }

//                readCharacteristic(SERVICE_ID, CHARACTERISTIC_READ)
                servicesAvailableBLE = gatt.services
                servicesAvailableBLE.forEach { service ->
                    val serviceUUIDHex = service.uuid.toString().toUpperCase(Locale.ROOT)
                    Log.d(TAG, "*********  UUID service: $serviceUUIDHex  **********")

                    service.characteristics.forEach { characteristic ->
                        val characteristicUUIDHex = characteristic.uuid.toString().toUpperCase(Locale.ROOT)
//                        Log.d(TAG, "Característica UUID: $characteristicUUIDHex, Service: $serviceUUIDHex")

                        characteristic.descriptors.forEach { descriptor ->
                            val descriptorUUIDHex = descriptor.uuid.toString().toUpperCase(Locale.ROOT)
                            Log.d(TAG, "Descriptor UUID: $descriptorUUIDHex, Característica UUID: $characteristicUUIDHex")
                        }
                    }
                }

                val service = gatt.getService(UUID.fromString(ROBOT_SERVICE_ID))
                val characteristicDynamicData = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_READ_NOTIFY_DYNAMIC_DATA))
                if (characteristicDynamicData != null) {
                    gatt.setCharacteristicNotification(characteristicDynamicData, true)
                    val descriptor = characteristicDynamicData.getDescriptor(UUID.fromString(ROBOT_SERVICE_DESCRIPTOR))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }

                val characteristicSettings = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_SETTINGS_READ))
                if (characteristicSettings != null) {
                    gatt.setCharacteristicNotification(characteristicSettings, true)
                    val descriptor = characteristicSettings.getDescriptor(UUID.fromString(ROBOT_SETTINGS_DESCRIPTOR))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                Log.d(TAG,"caracteristica leida uuid: ${characteristic?.uuid}")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                if(characteristic?.uuid != UUID.fromString("0000abf2-0000-1000-8000-00805f9b34fb")) {
                    Log.d("onCharacteristicChanged","uuid: ${characteristic?.uuid}, value: ${characteristic?.value?.toHex()}, size: ${characteristic?.value?.size}")
                }

                characteristic?.value?.let {
//                    Log.d(TAG, "Caracteristica leida: ${it.toHex()}, tamaño: ${it.size}")
                    if(it.size > 2) ioScope.launch { _receivedDataBtFlow.emit(it.toByteBuffer()) }     // TODO: definir tamaño
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
//                Log.v(TAG, "Write status: $status")
            }
        }

        bluetoothGatt = device.connectGatt(context,true,gattCallback)
    }

    @SuppressLint("MissingPermission")                     // TODO: manejar correctamente los permisos
    override fun startScan() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val leScanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                if (!discoveredDevices.contains(result.device)) {
                    discoveredDevices.add(result.device)
                    ioScope.launch { _availableBtDevices.emit(discoveredDevices) }
                }
            }
        }
        discoveredDevices.clear()

        if (!scanning) {
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                changeStatus(DISCONNECT)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
            changeStatus(DISCOVERING)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            changeStatus(DISCONNECT)
        }
    }

    override suspend fun disconnectDevice() {
        TODO("Not yet implemented")
    }

    override fun getDeviceConnected(): BluetoothDevice? {
        return deviceConnected
    }

    private fun changeStatus(newStatus: ConnectionStatus) {
        deviceConnected = bluetoothGatt?.device.takeIf { newStatus == CONNECTED }
        actualConnectionStatus = newStatus
        ioScope.launch { _connectionsStatus.emit(newStatus) }
    }

    override fun sendData(data: ByteArray) {                            // TODO: encolar envios
        writeCharacteristic(ROBOT_SERVICE_ID, CHARACTERISTIC_WRITE, data)
    }

    @SuppressLint("MissingPermission")                                  // TODO: manejar correctamente los permisos
    fun readCharacteristic(serviceName: String, characteristicName: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceName))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicName))

        if (characteristic != null) {
            val success = bluetoothGatt?.readCharacteristic(characteristic)
            Log.v(TAG, "Read status: $success")
        }
    }

    @SuppressLint("MissingPermission")                     // TODO: manejar correctamente los permisos
    private fun writeCharacteristic(serviceName: String,characteristicName: String, data: ByteArray) {

        val service = bluetoothGatt?.getService(UUID.fromString(serviceName))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicName))
        characteristic?.let {
            it.value = data
//            it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val success = bluetoothGatt?.writeCharacteristic(it)
            Log.d("bluetooth", "Write status: $success")
        }
    }
}

private const val SCAN_PERIOD: Long = 10000
private const val MTU_SIZE = 32

private const val ROBOT_SERVICE_ID = "0000abf0-0000-1000-8000-00805f9b34fb"
private const val ROBOT_SERVICE_DESCRIPTOR = "00002902-0000-1000-8000-00805F9B34FB"
private const val ROBOT_SETTINGS_DESCRIPTOR = "0000ABCD-0000-1000-8000-00805F9B34FB"

private const val CHARACTERISTIC_READ_NOTIFY_DYNAMIC_DATA = "0000abf2-0000-1000-8000-00805f9b34fb"
private const val CHARACTERISTIC_WRITE = "0000abf3-0000-1000-8000-00805f9b34fb"

private const val CHARACTERISTIC_SETTINGS_READ = "0000ABF9-0000-1000-8000-00805F9B34FB"

private const val CHARACTERISTIC_READ_NOTIFY_SETTINGS = "0000abf4-0000-1000-8000-00805f9b34fb"
//private const val CHARACTERISTIC_READ_NOTIFY = "0000abf4-0000-1000-8000-00805f9b34fb"