package com.example.hoverrobot.data.utils

import com.example.hoverrobot.R

object StatusMapperBT {
    private val mapStatusString = mapOf(
        ConnectionStatus.INIT to "Iniciando",
        ConnectionStatus.WAITING to "Esperando conexion",
        ConnectionStatus.CONNECTED to "Conectado",
        ConnectionStatus.ERROR to "Reintentar"
    )

    private val mapStatusColor = mapOf(
        ConnectionStatus.INIT to R.color.status_orange,
        ConnectionStatus.WAITING to R.color.status_turquesa,
        ConnectionStatus.CONNECTED to R.color.status_blue,
        ConnectionStatus.ERROR to R.color.status_red
    )

    private val mapStatusRobotString = mapOf(
        StatusEnumRobot.ROBOT_INIT to "Inicializando",
        StatusEnumRobot.ROBOT_DISABLE to "Deshabilitado",
        StatusEnumRobot.ROBOT_ENABLE to "Armado",
        StatusEnumRobot.ROBOT_STABILIZED to "Estabilizado",
        StatusEnumRobot.ROBOT_ERROR to "Error"
    )

    private val mapStatusRobotColor = mapOf(
        StatusEnumRobot.ROBOT_INIT to R.color.status_orange,
        StatusEnumRobot.ROBOT_DISABLE to R.color.gray_80_percent,
        StatusEnumRobot.ROBOT_ENABLE to R.color.status_turquesa,
        StatusEnumRobot.ROBOT_STABILIZED to R.color.status_blue,
        StatusEnumRobot.ROBOT_ERROR to R.color.status_red
    )

    fun mapStatusTostring(statusBt: ConnectionStatus, statusRobot: StatusEnumRobot?): String {
        return if (statusBt == ConnectionStatus.CONNECTED && statusRobot != null) {
            if (mapStatusRobotString.containsKey(statusRobot)) mapStatusRobotString[statusRobot]!! else "XXX"
        } else {
            if (mapStatusString.containsKey(statusBt)) mapStatusString[statusBt]!! else "XXX"
        }
    }

    fun mapStatusToColor(statusBt: ConnectionStatus, statusRobot: StatusEnumRobot?): Int {
        return if (statusBt == ConnectionStatus.CONNECTED) {
            if (mapStatusRobotColor.containsKey(statusRobot)) mapStatusRobotColor[statusRobot]!! else R.color.gray_80_percent
        } else {
            if (mapStatusColor.containsKey(statusBt)) mapStatusColor[statusBt]!! else R.color.gray_80_percent
        }
    }
}

enum class ConnectionStatus {
    INIT,
    WAITING,
    CONNECTED,
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