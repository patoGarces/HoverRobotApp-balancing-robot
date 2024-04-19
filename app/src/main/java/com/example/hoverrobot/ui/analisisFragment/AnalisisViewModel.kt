package com.example.hoverrobot.ui.analisisFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.Models.comms.MainBoardResponse

class AnalisisViewModel : ViewModel() {

    private var _newDataAnalisis : MutableLiveData<MainBoardResponse> = MutableLiveData()
    val newDataAnalisis : LiveData<MainBoardResponse> get() = _newDataAnalisis

    fun addNewPointData(newFrame : MainBoardResponse){
        _newDataAnalisis.postValue( newFrame )
    }
}