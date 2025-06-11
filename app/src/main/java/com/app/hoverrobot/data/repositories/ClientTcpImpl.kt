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
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class ClientTcpImpl() : SocketTcpInterface {

    var tcpSocket: Socket? = null

    private val _receivedDataFlow = MutableSharedFlow<ByteBuffer>()
    override val receivedDataFlow: SharedFlow<ByteBuffer> = _receivedDataFlow

    private val _connectionsStatus = MutableStateFlow(StatusConnection.DISCONNECTED)
    override val connectionsStatus: StateFlow<StatusConnection> = _connectionsStatus

    private var socketRunningJob: Job? = null

    private val TAG = "ClientTcp"

    var serverAddress: String? = null
        internal set

    var paquetsPerSecond: Int = 0
        internal set

    private var contPackets = 0

    init {
        setNewConnectStatus(StatusConnection.DISCONNECTED)
        measureReception()
    }

    override fun reconnect(serverIp: String, port: Int) {
        serverAddress = serverIp
        socketRunningJob?.cancel()
        initSocket(serverIp, port)
    }

    private fun initSocket(serverIp: String, port: Int = 8080) {
        socketRunningJob = ioScope.launch {
            while (isActive) {
                tcpSocket?.close()
                tcpSocket = null
                setNewConnectStatus(StatusConnection.SEARCHING)
                // Siempre crear una nueva instancia antes de intentar conectar
                tcpSocket = Socket()

                try {
                    tcpSocket?.connect(InetSocketAddress(serverIp, port), 5000)
                    tcpSocket?.soTimeout =
                        3000                         // Timeout para detectar la desconexion
                    setNewConnectStatus(StatusConnection.CONNECTED)

                    val input = BufferedInputStream(tcpSocket?.getInputStream())
                    input.handleReception()
                } catch (e: IOException) {
                    Log.e(TAG, "Error al conectar: ${e.message}", e)
                } finally {
                    tcpSocket?.close()
                }
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
            tcpSocket?.close()
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
            tcpSocket?.let { socket ->
                val outputStream: OutputStream = socket.getOutputStream()
                outputStream.write(data)
                outputStream.flush()
            }
        }
    }
}