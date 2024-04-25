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
        StatusEnumRobot.ROBOT_INIT to "Iniciando",
        StatusEnumRobot.ROBOT_DISABLE to "Deshabilitado",
        StatusEnumRobot.ROBOT_ENABLE to "Habilitado",
        StatusEnumRobot.ROBOT_STABILIZED to "Estabilizado",
        StatusEnumRobot.ROBOT_ERROR to "Error"
    )

    private val mapStatusRobotColor = mapOf(
        StatusEnumRobot.ROBOT_INIT to R.color.gray_80_percent,
        StatusEnumRobot.ROBOT_DISABLE to R.color.gray_80_percent,
        StatusEnumRobot.ROBOT_ENABLE to R.color.status_turquesa,
        StatusEnumRobot.ROBOT_STABILIZED to R.color.status_blue,
        StatusEnumRobot.ROBOT_ERROR to R.color.status_red
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
    ROBOT_INIT,
    ROBOT_DISABLE,
    ROBOT_ENABLE,
    ROBOT_STABILIZED,
    ROBOT_ERROR;

    companion object {
        fun getStatusRobot(code: Int): StatusEnumRobot? {
            val codeMap = entries.associateBy { it.ordinal }
            return codeMap[code]
        }
    }
}