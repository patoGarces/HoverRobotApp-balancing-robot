package com.example.hoverrobot.ui.analisisFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.repositories.CommsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _newDataAnalisis : MutableLiveData<RobotDynamicData> = MutableLiveData()
    val newDataAnalisis : LiveData<RobotDynamicData> get() = _newDataAnalisis

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect {
                _newDataAnalisis.postValue( it )
            }
        }
    }
}