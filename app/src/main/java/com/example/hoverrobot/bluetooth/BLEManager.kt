package com.example.hoverrobot.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
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
import java.util.UUID


interface BluetoothManagerInterface {

    fun isBluetoothEnabled(): Boolean

    fun startScan()

    fun connectDevice(device: BluetoothDevice)

    suspend fun disconnectDevice()

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
                    }
                    BluetoothGatt.STATE_CONNECTING -> { changeStatus(CONNECTING) }
                    BluetoothGatt.STATE_DISCONNECTED -> { changeStatus(DISCONNECT) }
                    BluetoothGatt.STATE_DISCONNECTING -> { changeStatus(DISCONNECT) }               // TOdO: evaluar agregar este estado
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                servicesAvailableBLE = gatt.services
                gatt.services.forEach{
                    Log.d(TAG,"servicios que ofrece mi esp32 uuid service: ${it.uuid}, uuid chararcteristic: ${it.characteristics}")
                }

                val service = gatt.getService(UUID.fromString("0000abf0-0000-1000-8000-00805f9b34fb"))
                val characteristic = service.getCharacteristic(UUID.fromString("0000abf3-0000-1000-8000-00805f9b34fb"))

                // Escribir en la característica
                writeCharacteristic(service, characteristic, "Hola mundooo")
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                if (characteristic.uuid == servicesAvailableBLE?.first()?.uuid) {                   // TODO: cambiar UUID a conveniencia
                    Log.v(TAG, "Write status: $status")
                }
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

//    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    private fun writeCharacteristic(service: BluetoothGattService,characteristic: BluetoothGattCharacteristic,value: String) {

        // First write the new value to our local copy of the characteristic
        characteristic.value = value.toByteArray()

        //...Then send the updated characteristic to the device
        val success = bluetoothGatt?.writeCharacteristic(characteristic)

        Log.v("bluetooth", "Write status: $success")

    }
}

private const val SCAN_PERIOD: Long = 10000