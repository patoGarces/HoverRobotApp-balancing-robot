package com.example.hoverrobot.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.ConnectionStatus.CONNECTED
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCOVERING
import com.example.hoverrobot.data.utils.ConnectionStatus.DISCONNECT
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer


interface BluetoothManagerInterface {

    fun isBluetoothEnabled(): Boolean

    fun startScan()
    fun connectDevice(device: BluetoothDevice): Boolean

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

    internal var actualConnectionStatus: ConnectionStatus = ConnectionStatus.INIT
    internal var deviceNameConnected: String? = null

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    override fun connectDevice(device: BluetoothDevice): Boolean {
        TODO("Not yet implemented")
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

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (!discoveredDevices.contains(result.device)) {
                discoveredDevices.add(result.device)
                ioScope.launch { _availableBtDevices.emit(discoveredDevices) }
            }
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
        ioScope.launch { _connectionsStatus.emit(DISCONNECT) }
    }
}