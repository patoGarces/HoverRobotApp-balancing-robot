package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.utils.StatusConnection
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer

sealed interface SocketMessage {
    data class Text(val value: String) : SocketMessage
    data class Binary(val data: ByteArray) : SocketMessage
}

interface RobotSocketInterface {
    val receivedDataFlow: SharedFlow<SocketMessage>
    val connectionsStatus: StateFlow<StatusConnection>

    var serverAddress: String?

    suspend fun reconnect(host: String, port: Int)

    suspend fun send(data: ByteArray)
}