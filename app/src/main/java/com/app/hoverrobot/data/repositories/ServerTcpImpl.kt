package com.app.hoverrobot.data.repositories

import android.util.Log
import com.app.hoverrobot.data.models.comms.SocketTcpInterface
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.data.utils.toByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer

class ServerTcpImpl(private val port: Int = 8080): SocketTcpInterface {

    val tcpSocket: ServerSocket

    private val _receivedDataFlow = MutableSharedFlow<ByteBuffer>()
    override val receivedDataFlow: SharedFlow<ByteBuffer> = _receivedDataFlow

    private val _connectionsStatus = MutableStateFlow(StatusConnection.INIT)
    override val connectionsStatus: StateFlow<StatusConnection> = _connectionsStatus

    private val TAG = "ServerTcp"

    private lateinit var socket: Socket // TODO: no deberia existir creo, alcanza con tcpSocket

    var clientsIp: String? = null
        internal set

    var paquetsPerSecond: Int = 0
        internal set

    private var contPackets = 0
    private var contFailReception = 0

    init {
        setNewConnectStatus(StatusConnection.INIT)
        tcpSocket = ServerSocket(port)
        socketHandler()
    }

    private fun socketHandler() {
        ioScope.launch {
            while (true) {
                setNewConnectStatus(StatusConnection.WAITING)
                socket = tcpSocket.accept()

                val remoteIp = socket.inetAddress
                remoteIp.hostAddress?.let { clientIp ->
                    clientsIp = clientIp
                    Log.d(TAG, "remote ip: $clientIp, name: ${remoteIp.hostName}")
                }

                ioScope.launch { socketAlive() }

                setNewConnectStatus(StatusConnection.CONNECTED)

                if (!socket.isClosed) socket.getInputStream().handleReception()

                socket.close()
            }
        }
    }

    private suspend fun InputStream.handleReception() {
        val buffer = ByteArray(1024) // Tamaño del buffer para leer los datos
        var bytesRead: Int

        while (true) {
            try {
                bytesRead = withContext(Dispatchers.IO) {
                    read(buffer)
                }
            }
            catch (e: SocketException) {
                Log.e(TAG, "Error socket: $e")
                break
            }

            if (bytesRead > 0) {
                contPackets++
                val receivedData = buffer.copyOf(bytesRead)
                _receivedDataFlow.emit(receivedData.toByteBuffer())
            }
            else if(bytesRead < 0) {
                Log.e(TAG, "Error bytesRead")
                break
            }
        }
    }

    private suspend fun socketAlive() {
        while (socket.isConnected) {
            if (contPackets != 0) {
                paquetsPerSecond = contPackets
                contPackets = 0
            }
            else {
                contFailReception++
                if(contFailReception > 3) {
                    Log.e(TAG,"Force close socket because contFailReception")
                    contFailReception = 0
                    socket.close()
                    break
                }
            }
            delay(1000)
        }
    }

    private fun setNewConnectStatus(newStatus: StatusConnection) {
        ioScope.launch {
            _connectionsStatus.emit(newStatus)
        }
    }

    fun sendData(data: ByteArray) {
        ioScope.launch {
            socket.let {
                val outputStream: OutputStream = socket.getOutputStream()
                outputStream.write(data)
                outputStream.flush()
            }
        }
    }
}