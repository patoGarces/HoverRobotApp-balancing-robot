package com.example.hoverrobot.bluetooth

import android.bluetooth.BluetoothDevice
import java.nio.ByteBuffer

interface BluetoothInterface {

    fun getStatusBT() : StatusEnumBT
    fun setStatusBT( status: StatusEnumBT)
    fun getDevicesBT( devices: List<BluetoothDevice>)
    fun initDiscover()
    fun stopDiscover()

    fun newMessageReceive( buffer : ByteBuffer)
}