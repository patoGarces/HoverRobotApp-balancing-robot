package com.app.hoverrobot.data.repositories

import com.app.hoverrobot.data.models.comms.RobotSocketInterface
import com.app.hoverrobot.data.utils.StatusConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.milliseconds

class RobotWebSocketClient(private val httpClient: HttpClient, ): RobotSocketInterface {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sessionMutex = Mutex()
    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null

    private val _receivedDataFlow = MutableSharedFlow<ByteBuffer>()
    override val receivedDataFlow: SharedFlow<ByteBuffer> = _receivedDataFlow

    private val _connectionsStatus = MutableStateFlow(StatusConnection.DISCONNECTED)
    override val connectionsStatus: StateFlow<StatusConnection> = _connectionsStatus

    private val _messages = MutableSharedFlow<String>(          // TODO: ver que hacer para recibir los json
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages = _messages.asSharedFlow()

    /**
     * Starts connecting in the background.
     * Reconnects automatically until disconnect() is called.
     */
    override suspend fun reconnect(host: String, port: Int) {
//        require(host.startsWith("ws://") || host.startsWith("wss://")) {
//            "URL must start with ws:// or wss://"
//        }
        val url = "ws://$host:$port"

        disconnect()

        connectionJob = scope.launch {
            var attempt = 0

            while (true) {
                var activeSession: DefaultClientWebSocketSession? = null

                try {
                    _connectionsStatus.value = if (attempt == 0) {
                        StatusConnection.CONNECTING
                    } else {
                        StatusConnection.SEARCHING
                    }

                    activeSession = httpClient.webSocketSession {
                        url(url)
                    }

                    sessionMutex.withLock {
                        session = activeSession
                    }

                    _connectionsStatus.value = StatusConnection.CONNECTED

                    attempt = 0

                    for (frame in activeSession.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                _messages.emit(frame.readText())
                            }

                            is Frame.Binary -> {
                                val bytes = frame.readBytes()
                                _receivedDataFlow.emit(ByteBuffer.wrap(bytes))
                            }

                            else -> Unit
                        }
                    }

                    throw io.ktor.utils.io.errors.IOException("WebSocket closed by server")

                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    attempt++

                    _connectionsStatus.value = StatusConnection.SEARCHING
                } finally {
                    withContext(NonCancellable) {
                        sessionMutex.withLock {
                            if (session === activeSession) {
                                session = null
                            }
                        }

                        runCatching {
                            activeSession?.close()
                        }
                    }
                }

                // 1, 2, 4, 8, 16, then 30 seconds maximum.
                val backoff = (1_000L * (1L shl (attempt - 1).coerceIn(0, 4)))
                    .coerceAtMost(30_000L)

                delay(backoff.milliseconds)
            }
        }
    }

    /**
     * Sends immediately; never queues commands while disconnected.
     */
    override suspend fun send(data: ByteArray) {
        sessionMutex.withLock {
            val current = session
                ?: throw IllegalStateException("WebSocket is not connected")

            current.send(Frame.Binary(fin = true, data = data))
        }
    }

    suspend fun disconnect() {
        val job = connectionJob
        connectionJob = null

        job?.cancelAndJoin()

        withContext(NonCancellable) {
            sessionMutex.withLock {
                runCatching {
                    session?.close()
                }
                session = null
            }
        }

        _connectionsStatus.value = StatusConnection.DISCONNECTED
    }

    /**
     * Call when the owner (e.g. a ViewModel or DI container) is finished
     * with this client. The injected HttpClient remains externally owned.
     */
    suspend fun dispose() {
        disconnect()
        scope.cancel()
    }
}