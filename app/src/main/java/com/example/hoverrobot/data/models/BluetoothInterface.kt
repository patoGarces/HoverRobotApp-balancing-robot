package com.example.hoverrobot.data.models

import android.bluetooth.BluetoothDevice
import com.example.hoverrobot.data.models.comms.MainBoardRobotStatus
import com.example.hoverrobot.data.utils.StatusEnumBT
import java.nio.ByteBuffer

interface BluetoothInterface {
    fun getStatusBT(): StatusEnumBT
    fun setStatusBT(status: StatusEnumBT)
    fun getDevicesBT(devices: List<BluetoothDevice>)
    fun initDiscover()
    fun stopDiscover()
}