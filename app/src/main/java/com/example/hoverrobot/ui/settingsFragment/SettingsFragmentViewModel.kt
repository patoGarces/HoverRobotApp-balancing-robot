package com.example.hoverrobot.ui.settingsFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.Models.comms.PidSettings

class SettingsFragmentViewModel: ViewModel(){

    private var _pidSettingToRobot: MutableLiveData<PidSettings?> = MutableLiveData()
    val pidSettingToRobot : LiveData<PidSettings?> get() = _pidSettingToRobot

    private var _pidSettingFromRobot: MutableLiveData<PidSettings?> = MutableLiveData()
    val pidSettingFromRobot : LiveData<PidSettings?> get() = _pidSettingFromRobot

    init {
        _pidSettingToRobot.value = null
        _pidSettingFromRobot.value = null
    }

    fun setPidTunningToRobot(newTunning : PidSettings){
        _pidSettingToRobot.postValue( newTunning )
    }

    fun setPidTunningfromRobot(newTunning : PidSettings){
        _pidSettingFromRobot.postValue( newTunning )
    }
}