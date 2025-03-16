package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.utils.StatusConnection

data class ConnectionState(
    val status: StatusConnection = StatusConnection.WAITING,
    val receiverPacketRates: Int = 0,
    val rssi: Int = 0,
    val strength: Int = 0,
    val frequency: Int = 0,
    val ip: String? = null
)
