package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.utils.ConnectionStatus

class BottomSheetDevicesViewModel : ViewModel() {

    private var _devicesList : MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val deviceslist : LiveData<List<BluetoothDevice>> get() = _devicesList

    private var _deviceSelected : MutableLiveData<BluetoothDevice> = MutableLiveData()
    val deviceSelected: LiveData<BluetoothDevice> get() = _deviceSelected

    private var _statusBtnRefresh : MutableLiveData<ConnectionStatus> = MutableLiveData()
    val statusBtnRefresh: LiveData<ConnectionStatus> get() = _statusBtnRefresh

    init {
        _devicesList.value = null                                       // TODO: revisar
        _deviceSelected.value = null
        _statusBtnRefresh.value = null
    }

    fun newDeviceSelected( selectedDevice : BluetoothDevice){
        _deviceSelected.value = selectedDevice
    }
    fun updateDevicesList( devicesList : List<BluetoothDevice>){
        _devicesList.value = devicesList
    }

    fun updateStatusBtnRefresh(connectionStatus: ConnectionStatus){
        _statusBtnRefresh.value = connectionStatus
    }
}