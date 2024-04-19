package com.example.hoverrobot.ui.statusDataFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusDataViewModel : ViewModel() {

    private var _gralStatus = MutableLiveData<Int>()
    val gralStatus : LiveData<Int> = _gralStatus

    private var _escsTemp = MutableLiveData<Float>()
    val escsTemp : LiveData<Float> = _escsTemp

    private var _imuTemp = MutableLiveData<Float>()
    val imuTemp : LiveData<Float> = _imuTemp


    init{
        _gralStatus.postValue(StatusEnumGral.STATUS_UNKNOWN.ordinal)
        _escsTemp.postValue(0F)
        _imuTemp.postValue(0F)
    }

    fun setEscsTemp(temp : Float){
        _escsTemp.postValue( temp )
    }

    fun setImuTemp(temp : Float){
        _imuTemp.postValue( temp )
    }

    fun setGralStatus(status : Short){
        _gralStatus.postValue( status.toInt() )
    }
}