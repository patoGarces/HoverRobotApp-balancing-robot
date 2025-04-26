package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.utils.StatusConnection
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer

interface SocketTcpInterface {
    val receivedDataFlow: SharedFlow<ByteBuffer>
    val connectionsStatus: StateFlow<StatusConnection>
}