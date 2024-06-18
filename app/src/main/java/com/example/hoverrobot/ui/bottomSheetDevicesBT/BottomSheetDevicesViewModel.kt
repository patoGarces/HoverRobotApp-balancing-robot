package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.utils.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomSheetDevicesViewModel @Inject constructor(
    private val commsRepository: CommsRepository
) : ViewModel() {

    private var _devicesList : MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val deviceslist : LiveData<List<BluetoothDevice>> get() = _devicesList

    private var _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus : LiveData<ConnectionStatus> = _connectionStatus

    init {
        ioScope.launch {
            commsRepository.availableDevices.collect {
                _devicesList.postValue(it)
            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _connectionStatus.postValue(it)
            }
        }
    }

    fun newDeviceSelected( selectedDevice : BluetoothDevice){
        commsRepository.connectDevice(selectedDevice)
    }

    fun retryDiscover() {
        commsRepository.startDiscoverBT()
    }
}