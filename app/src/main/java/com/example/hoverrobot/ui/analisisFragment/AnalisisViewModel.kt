package com.example.hoverrobot.ui.analisisFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.MainBoardRobotStatus

class AnalisisViewModel : ViewModel() {

    private var _newDataAnalisis : MutableLiveData<MainBoardRobotStatus> = MutableLiveData()
    val newDataAnalisis : LiveData<MainBoardRobotStatus> get() = _newDataAnalisis

    fun addNewPointData(newFrame : MainBoardRobotStatus){
        _newDataAnalisis.postValue( newFrame )
    }
}