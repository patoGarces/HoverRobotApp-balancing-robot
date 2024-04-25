package com.example.hoverrobot.ui.analisisFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoverrobot.data.models.comms.MainBoardRobotStatus
import com.example.hoverrobot.data.repository.CommsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private var _newDataAnalisis : MutableLiveData<MainBoardRobotStatus> = MutableLiveData()
    val newDataAnalisis : LiveData<MainBoardRobotStatus> get() = _newDataAnalisis

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        ioScope.launch {
            commsRepository.statusRobotFlow.collect {
                _newDataAnalisis.postValue( it )
            }
        }
    }
}