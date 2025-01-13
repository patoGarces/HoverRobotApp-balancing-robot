package com.example.hoverrobot.data.utils

import com.example.hoverrobot.R

object StatusMapper {
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
        StatusRobot.ROBOT_INIT to "Inicializando",
        StatusRobot.ROBOT_DISABLE to "Deshabilitado",
        StatusRobot.ROBOT_ENABLE to "Armado",
        StatusRobot.ROBOT_STABILIZED to "Estabilizado",
        StatusRobot.ROBOT_ERROR to "Error"
    )

    private val mapStatusRobotColor = mapOf(
        StatusRobot.ROBOT_INIT to R.color.status_orange,
        StatusRobot.ROBOT_DISABLE to R.color.gray_80_percent,
        StatusRobot.ROBOT_ENABLE to R.color.status_turquesa,
        StatusRobot.ROBOT_STABILIZED to R.color.status_blue,
        StatusRobot.ROBOT_ERROR to R.color.status_red
    )

    fun statusToString(statusBt: ConnectionStatus, statusRobot: StatusRobot? = null): String {
        return if (statusBt == ConnectionStatus.CONNECTED && statusRobot != null) {
            if (mapStatusRobotString.containsKey(statusRobot)) mapStatusRobotString[statusRobot]!! else "XXX"
        } else {
            if (mapStatusString.containsKey(statusBt)) mapStatusString[statusBt]!! else "XXX"
        }
    }

    fun statusToColor(statusBt: ConnectionStatus, statusRobot: StatusRobot? = null): Int {
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

enum class StatusRobot {
    ROBOT_INIT,
    ROBOT_DISABLE,
    ROBOT_ENABLE,
    ROBOT_STABILIZED,
    ROBOT_ERROR;

    companion object {
        fun getStatusRobot(code: Int): StatusRobot? {
            val codeMap = entries.associateBy { it.ordinal }
            return codeMap[code]
        }
    }
}