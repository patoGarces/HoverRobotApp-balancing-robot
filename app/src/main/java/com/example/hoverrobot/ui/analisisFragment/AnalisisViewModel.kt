package com.example.hoverrobot.ui.analisisFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.data.models.comms.MainBoardRobotStatus
import com.example.hoverrobot.data.repositories.CommsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _newDataAnalisis : MutableLiveData<MainBoardRobotStatus> = MutableLiveData()
    val newDataAnalisis : LiveData<MainBoardRobotStatus> get() = _newDataAnalisis

    init {
        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _newDataAnalisis.postValue( it )
            }
        }
    }
}