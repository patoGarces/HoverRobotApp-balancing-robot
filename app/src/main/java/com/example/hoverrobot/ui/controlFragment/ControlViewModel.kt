package com.example.hoverrobot.ui.controlFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.Models.comms.AxisControl

class ControlViewModel : ViewModel() {

    private var _controlAxis : MutableLiveData<AxisControl?> = MutableLiveData()
    val controlAxis : LiveData<AxisControl?> get() = _controlAxis

    private var _joyVisible : MutableLiveData<Boolean?> = MutableLiveData()
    val joyVisible : LiveData<Boolean?> get() = _joyVisible

    init{
        _controlAxis.value = null
    }

    fun setVisibility(visible : Boolean){
        _joyVisible.value = visible
    }

    fun newCoordinatesJoystick(newAxisControl: AxisControl){
        _controlAxis.value = newAxisControl
    }
}