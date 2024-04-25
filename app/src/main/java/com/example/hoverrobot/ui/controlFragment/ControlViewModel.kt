package com.example.hoverrobot.ui.controlFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.data.repository.CommsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _joyVisible : MutableLiveData<Boolean?> = MutableLiveData()
    val joyVisible : LiveData<Boolean?> get() = _joyVisible


    fun setVisibility(visible : Boolean){
        _joyVisible.value = visible
    }

    fun newCoordinatesJoystick(newAxisControl: AxisControl){
        commsRepository.sendJoystickUpdate(newAxisControl)
    }
}