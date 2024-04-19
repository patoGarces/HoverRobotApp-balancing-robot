package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomSheetDevicesViewModel : ViewModel() {

    private var _devicesList : MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val deviceslist : LiveData<List<BluetoothDevice>> get() = _devicesList

    private var _deviceSelected : MutableLiveData<BluetoothDevice> = MutableLiveData()
    val deviceSelected: LiveData<BluetoothDevice> get() = _deviceSelected

    private var _statusBtnRefresh : MutableLiveData<StatusViewBt> = MutableLiveData()
    val statusBtnRefresh: LiveData<StatusViewBt> get() = _statusBtnRefresh

    init {
        _devicesList.value = null
        _deviceSelected.value = null
        _statusBtnRefresh.value = null
    }

    fun newDeviceSelected( selectedDevice : BluetoothDevice){
        _deviceSelected.value = selectedDevice
    }
    fun updateDevicesList( devicesList : List<BluetoothDevice>){
        _devicesList.value = devicesList
    }

    fun updateStatusBtnRefresh( status : StatusViewBt){
        _statusBtnRefresh.value = status
    }
}