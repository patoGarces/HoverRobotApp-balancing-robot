package com.example.hoverrobot.data.repositories

import android.util.Log
import com.example.hoverrobot.data.utils.ByteArraysUtils.toByteBuffer
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import kotlinx.coroutines.CoroutineScope
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
import java.nio.ByteBuffer


class ServerTcp {

    private val tcpSocket: ServerSocket

    private val _receivedDataFlow = MutableSharedFlow<ByteBuffer>()
    val receivedDataFlow: SharedFlow<ByteBuffer> = _receivedDataFlow

    private val _connectionsStatus = MutableStateFlow(ConnectionStatus.INIT)
    val connectionsStatus: StateFlow<ConnectionStatus> = _connectionsStatus

    private val TAG = "ServerTcp"

    private val port = 8080

    private var contPackets: Long = 0

    var localIp = ""
        internal set

    var clientsIp: MutableList<String>? = null
        internal set

    var paquetsPerSecond: Long = 0
        internal set

    private lateinit var socket: Socket

    init {
        setNewConnectStatus(ConnectionStatus.INIT)

        tcpSocket = ServerSocket(port)

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                paquetsPerSecond = contPackets
                contPackets = 0
                delay(1000)
            }
        }
        ioScope.launch {
            while (true) {
                Log.d(TAG, "Server started, waiting clients")
                setNewConnectStatus(ConnectionStatus.DISCOVERING)
                socket = tcpSocket.accept()

                val remoteIp = socket.inetAddress
                remoteIp.hostAddress?.let { clientIp ->
                    clientsIp?.add(clientIp)
                    Log.d(TAG, "remote ip: $clientIp, name: ${remoteIp.hostName}")
                }

                localIp = socket.localAddress.hostAddress
                Log.d(TAG, "local ip: $localIp, name ${socket.localAddress.hostName}")

                setNewConnectStatus(ConnectionStatus.CONNECTED)

                val input: InputStream = socket.getInputStream()

                input.handleReception()

                Log.e(TAG, "Error close socket and input")
                input.close()
                socket.close()
            }
        }
    }

    private suspend fun InputStream.handleReception() {
        val buffer = ByteArray(1024) // Tamaño del buffer para leer los datos
        var bytesRead: Int

        while (true) {
            bytesRead = withContext(Dispatchers.IO) {
                read(buffer)
            }

            if (bytesRead == -1) {
                Log.e(TAG, "Error bytesRead")
                break
            }

            if (bytesRead > 0) {
                contPackets++
                val receivedData = buffer.copyOf(bytesRead)
                _receivedDataFlow.emit(receivedData.toByteBuffer())
//                Log.d(TAG, "Received len: $bytesRead, paquet: ${receivedData.contentToString()}")
            }
        }
    }

    private fun setNewConnectStatus(newStatus: ConnectionStatus) {
        ioScope.launch {
            _connectionsStatus.emit(newStatus)
        }
    }


    fun sendData(data: ByteArray) {
        Log.d(TAG, "Data a enviar: ${String(data)}")

        ioScope.launch {
            socket.let {
                val outputStream: OutputStream = socket.getOutputStream()

                // Enviar datos al cliente
                outputStream.write(data)
                outputStream.flush()

            }
        }
    }

}