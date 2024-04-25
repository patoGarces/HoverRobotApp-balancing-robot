package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.hoverrobot.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.repository.CommsRepository
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

    private var _statusBtnRefresh : MutableLiveData<ConnectionStatus> = MutableLiveData()
    val statusBtnRefresh: LiveData<ConnectionStatus> get() = _statusBtnRefresh

    init {
        ioScope.launch {
            commsRepository.availableDevices.collect {
                _devicesList.postValue(it)
            }
        }

        ioScope.launch {
            commsRepository.connectionStateFlow.collect {
                _statusBtnRefresh.postValue(it)
            }
        }
    }

    fun newDeviceSelected( selectedDevice : BluetoothDevice){
        commsRepository.connectDevice(selectedDevice)
    }
}