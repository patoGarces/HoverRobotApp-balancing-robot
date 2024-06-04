package com.example.hoverrobot.bluetooth

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
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.ConnectionStatus.CONNECTED
import com.example.hoverrobot.data.utils.ConnectionStatus.CONNECTING
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCOVERING
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCONNECT
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

    fun getDeviceConnectedName(): String?
}


class BLEManager(private val context: Context): BluetoothManagerInterface {

    private val TAG = "BLEManager"

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

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
    internal var deviceNameConnected: String? = null
    internal var servicesAvailableBLE: List<BluetoothGattService>? = emptyList()

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    override fun connectDevice(device: BluetoothDevice) {

//        if (ActivityCompat.checkSelfPermission(           // TODO: manejar correctamente los permisos
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }

        val gattCallback = object: BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG,"Error GATT callback: $status")
                    return
                }

                when(newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        changeStatus(CONNECTED)
                        bluetoothGatt?.discoverServices()
                        Log.d(TAG,"CONECTADO")
                    }
                    BluetoothGatt.STATE_CONNECTING -> { changeStatus(CONNECTING) }
                    BluetoothGatt.STATE_DISCONNECTED -> { changeStatus(DISCONNECT) }
                    BluetoothGatt.STATE_DISCONNECTING -> { changeStatus(DISCONNECT) }               // TOdO: evaluar agregar este estado
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
//                readCharacteristicNotify(SERVICE_ID, CHARACTERISTIC_READ_NOTIFY)
                val servicesAvailableBLE = gatt.services
                servicesAvailableBLE.forEach { service ->
                    val serviceUUIDHex = service.uuid.toString().toUpperCase(Locale.ROOT)
                    Log.d(TAG, "UUID service: $serviceUUIDHex")

                    service.characteristics.forEach { characteristic ->
                        val characteristicUUIDHex = characteristic.uuid.toString().toUpperCase(Locale.ROOT)
                        Log.d(TAG, "Característica UUID: $characteristicUUIDHex, Service: $serviceUUIDHex")

                        characteristic.descriptors.forEach { descriptor ->
                            val descriptorUUIDHex = descriptor.uuid.toString().toUpperCase(Locale.ROOT)
                            Log.d(TAG, "Descriptor UUID: $descriptorUUIDHex, Característica UUID: $characteristicUUIDHex")
                        }
                    }
                }

                val service = gatt.getService(UUID.fromString(SERVICE_ID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_READ_NOTIFY))
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"))
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

                Log.d(TAG,"Caracteristica leida: ${characteristic}")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.d(TAG,"2 Caracteristica leida: ${characteristic?.value.contentToString()}")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)

                Log.d(TAG,"on characteristicChanged: $value")
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
//                if (characteristic.uuid == servicesAvailableBLE?.first()?.uuid) {                   // TODO: cambiar UUID a conveniencia
                    Log.v(TAG, "Write status: $status")
//                }
            }
        }

        bluetoothGatt = device.connectGatt(context,true,gattCallback)
    }

    override fun startScan() {

//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
            // TODO: Enviar solicitud a la activity para solicitar permiso de SCAN
//            Log.e(TAG,"No hay permiso de BLUETOOTH_SCAN")
//            return
//        }

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

        if (!scanning) { // Stops scanning after a pre-defined scan period.
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

    override fun getDeviceConnectedName(): String? {
        return deviceNameConnected
    }

    private fun changeStatus(newStatus: ConnectionStatus) {
        actualConnectionStatus = newStatus
        ioScope.launch { _connectionsStatus.emit(newStatus) }
    }

    override fun sendData(data: ByteArray) {           // TODO: encolar envios
        val serviceName = SERVICE_ID
        val characteristicName = CHARACTERISTIC_WRITE
        writeCharacteristic(serviceName, characteristicName, data)
    }

//    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readCharacteristic(serviceName: String, characteristicName: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceName))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicName))

        if (characteristic != null) {
            val success = bluetoothGatt?.readCharacteristic(characteristic)
            Log.v(TAG, "Read status: $success")
        }
    }

    private fun readCharacteristicNotify(serviceName: String, characteristicName: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceName))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicName))
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)

//        val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
//        val desc = characteristic?.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
//        desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//        bluetoothGatt?.writeDescriptor(desc)
    }

    private fun writeCharacteristic(serviceName: String,characteristicName: String, data: ByteArray) {

        val service = bluetoothGatt?.getService(UUID.fromString(serviceName))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicName))

//        //...Then send the updated characteristic to the device
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
        characteristic?.value = data
        val success = bluetoothGatt?.writeCharacteristic(characteristic)
        Log.v("bluetooth", "Write status: $success")
    }
}

private const val SCAN_PERIOD: Long = 10000

private const val SERVICE_ID = "0000abf0-0000-1000-8000-00805f9b34fb"
private const val CHARACTERISTIC_READ = "0000abf1-0000-1000-8000-00805f9b34fb"
private const val CHARACTERISTIC_READ_NOTIFY = "0000abf2-0000-1000-8000-00805f9b34fb"
private const val CHARACTERISTIC_WRITE = "0000abf3-0000-1000-8000-00805f9b34fb"
//private const val CHARACTERISTIC_READ_NOTIFY = "0000abf4-0000-1000-8000-00805f9b34fb"