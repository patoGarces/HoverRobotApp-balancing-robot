package com.example.hoverrobot.data.utils

import com.example.hoverrobot.R

object StatusMapperBT {
    private val mapStatusBtString = mapOf(
        ConnectionStatus.DISCONNECT to "Desconectado",
        ConnectionStatus.INIT to "Iniciando",
        ConnectionStatus.DISCOVERING to "Discovering",
        ConnectionStatus.CONNECTING to "Conectando",
        ConnectionStatus.CONNECTED to "Conectado",
        ConnectionStatus.ERROR to "Reintentar"
    )

    private val mapStatusBtColor = mapOf(
        ConnectionStatus.DISCONNECT to R.color.status_red,
        ConnectionStatus.INIT to R.color.status_orange,
        ConnectionStatus.DISCOVERING to R.color.status_turquesa,
        ConnectionStatus.CONNECTING to R.color.status_blue,
        ConnectionStatus.CONNECTED to R.color.gray_80_percent,
        ConnectionStatus.ERROR to R.color.status_red
    )

    private val mapStatusRobotString = mapOf(
        StatusEnumRobot.STATUS_ROBOT_INIT to "Iniciando",
        StatusEnumRobot.STATUS_ROBOT_DISABLE to "Deshabilitado",
        StatusEnumRobot.STATUS_ROBOT_ENABLE to "Habilitado",
        StatusEnumRobot.STATUS_ROBOT_STABILIZED to "Estabilizado",
        StatusEnumRobot.STATUS_ROBOT_ERROR to "Error"
    )

    private val mapStatusRobotColor = mapOf(
        StatusEnumRobot.STATUS_ROBOT_INIT to R.color.gray_80_percent,
        StatusEnumRobot.STATUS_ROBOT_DISABLE to R.color.gray_80_percent,
        StatusEnumRobot.STATUS_ROBOT_ENABLE to R.color.status_turquesa,
        StatusEnumRobot.STATUS_ROBOT_STABILIZED to R.color.status_blue,
        StatusEnumRobot.STATUS_ROBOT_ERROR to R.color.status_red
    )

    fun mapStatusTostring(statusBt: ConnectionStatus, statusRobot: StatusEnumRobot?): String {
        return if (statusBt == ConnectionStatus.CONNECTED && statusRobot != null) {
            if (mapStatusRobotString.containsKey(statusRobot)) mapStatusRobotString[statusRobot]!! else "XXX"
        } else {
            if (mapStatusBtString.containsKey(statusBt)) mapStatusBtString[statusBt]!! else "XXX"
        }
    }

    fun mapStatusToColor(statusBt: ConnectionStatus, statusRobot: StatusEnumRobot?): Int {
        return if (statusBt == ConnectionStatus.CONNECTED) {
            if (mapStatusRobotColor.containsKey(statusRobot)) mapStatusRobotColor[statusRobot]!! else R.color.gray_80_percent
        } else {
            if (mapStatusBtColor.containsKey(statusBt)) mapStatusBtColor[statusBt]!! else R.color.gray_80_percent
        }
    }
}

enum class ConnectionStatus {
    INIT,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    DISCONNECT,
    ERROR
}

enum class StatusEnumRobot {
    STATUS_ROBOT_INIT,
    STATUS_ROBOT_DISABLE,
    STATUS_ROBOT_ENABLE,
    STATUS_ROBOT_STABILIZED,
    STATUS_ROBOT_ERROR
}