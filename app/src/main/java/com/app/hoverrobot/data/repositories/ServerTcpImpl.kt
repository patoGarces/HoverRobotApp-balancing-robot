package com.app.hoverrobot.data.repositories

import android.util.Log
import com.app.hoverrobot.data.models.comms.SocketTcpInterface
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.data.utils.toByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class ServerTcpImpl(): SocketTcpInterface {

    var tcpSocket: ServerSocket? = null

    private val _receivedDataFlow = MutableSharedFlow<ByteBuffer>()
    override val receivedDataFlow: SharedFlow<ByteBuffer> = _receivedDataFlow

    private val _connectionsStatus = MutableStateFlow(StatusConnection.DISCONNECTED)
    override val connectionsStatus: StateFlow<StatusConnection> = _connectionsStatus

    private var socketRunningJob: Job? = null

    private val TAG = "ServerTcp"

    private var socket: Socket? = null

    var clientsIp: String? = null
        internal set

    var paquetsPerSecond: Int = 0
        internal set

    private var contPackets = 0

    init {
        setNewConnectStatus(StatusConnection.DISCONNECTED)
        measureReception()
    }

    override fun reconnect(serverIp: String, port: Int) {
        socketRunningJob?.cancel()
        initSocket(port)
    }

    private fun initSocket(port: Int) {
        socketRunningJob = ioScope.launch {
            while (isActive) {
                tcpSocket?.close()
                tcpSocket = null
                setNewConnectStatus(StatusConnection.WAITING)
                tcpSocket = ServerSocket(port)
                socket = tcpSocket?.accept()

                if (socket == null) {
                    Log.e(TAG, "Accept() returned null socket")
                    continue
                }

                val remoteIp = socket?.inetAddress
                remoteIp?.hostAddress?.let { clientIp ->
                    clientsIp = clientIp
                    Log.d(TAG, "remote ip: $clientIp, name: ${remoteIp.hostName}")
                }

                setNewConnectStatus(StatusConnection.CONNECTED)

                if (tcpSocket?.isClosed == false) socket?.getInputStream()?.handleReception()

                socket?.close()
            }
        }
    }

    private suspend fun InputStream.handleReception() {
        val buffer = ByteArray(1024)
        var bytesRead: Int

        try {
            while (true) {
                try {
                    bytesRead = withContext(Dispatchers.IO) {
                        read(buffer)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error de lectura o socket: $e")
                    break
                }

                if (bytesRead > 0) {
                    contPackets++
                    val receivedData = buffer.copyOf(bytesRead)
                    _receivedDataFlow.emit(receivedData.toByteBuffer())
                } else if (bytesRead < 0) {
                    break
                }
            }
        } finally {
            socket?.close()
        }
    }

    private fun measureReception() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                paquetsPerSecond = contPackets
                contPackets = 0
            }
        }, 0, 1000)
    }

    private fun setNewConnectStatus(newStatus: StatusConnection) {
        ioScope.launch {
            _connectionsStatus.emit(newStatus)
        }
    }

    fun sendData(data: ByteArray) {
        ioScope.launch {
            socket?.let { sock ->
                if (sock.isConnected == true) {
                    val outputStream: OutputStream = sock.getOutputStream()
                    outputStream.write(data)
                    outputStream.flush()
                }
            }
        }
    }
}