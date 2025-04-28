package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.utils.StatusConnection

data class ConnectionState(
    val status: StatusConnection = StatusConnection.SEARCHING,
    val receiverPacketRates: Int = 0,
    val addressIp: String? = null
)

data class NetworkState (
    val status: StatusConnection = StatusConnection.SEARCHING,
    val statusRobotClient: ConnectionState = ConnectionState(),
    val statusRaspiClient: ConnectionState = ConnectionState(),
    val rssi: Int = 0,
    val strength: Int = 0,
    val frequency: Int = 0,
    val localIp: String? = null
)