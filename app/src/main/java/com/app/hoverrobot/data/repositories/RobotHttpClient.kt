package com.app.hoverrobot.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets

fun createRobotHttpClient(): HttpClient =
    HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 20_000
            maxFrameSize = 1024L * 1024L
        }
    }