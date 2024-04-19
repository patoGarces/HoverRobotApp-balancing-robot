package com.example.hoverrobot.bluetooth

import com.example.hoverrobot.R

object StatusMapperBT {
    val mapStatusBtString = mapOf(
        StatusEnumBT.STATUS_DISCONNECT to "Desconectado",
        StatusEnumBT.STATUS_INIT to "Iniciando",
        StatusEnumBT.STATUS_DISCOVERING to "Discovering",
        StatusEnumBT.STATUS_CONNECTING to "Conectando",
        StatusEnumBT.STATUS_CONNECTED to "Conectado",
        StatusEnumBT.STATUS_ERROR to "Reintentar"
    )

    val mapStatusBtColor = mapOf(
        StatusEnumBT.STATUS_DISCONNECT to R.color.status_red,
        StatusEnumBT.STATUS_INIT to R.color.gray_80_percent,
        StatusEnumBT.STATUS_DISCOVERING to R.color.status_turquesa,
        StatusEnumBT.STATUS_CONNECTING to R.color.status_blue,
        StatusEnumBT.STATUS_CONNECTED to R.color.gray_80_percent,
        StatusEnumBT.STATUS_ERROR to R.color.status_red
    )

    val mapStatusRobotString = mapOf(
        StatusEnumRobot.STATUS_ROBOT_INIT to "Iniciando",
        StatusEnumRobot.STATUS_ROBOT_DISABLE to "Deshabilitado",
        StatusEnumRobot.STATUS_ROBOT_ENABLE to "Habilitado",
        StatusEnumRobot.STATUS_ROBOT_STABILIZED to "Estabilizado",
        StatusEnumRobot.STATUS_ROBOT_ERROR to "Error"
    )

    val mapStatusRobotColor = mapOf(
        StatusEnumRobot.STATUS_ROBOT_INIT to R.color.gray_80_percent,
        StatusEnumRobot.STATUS_ROBOT_DISABLE to R.color.gray_80_percent,
        StatusEnumRobot.STATUS_ROBOT_ENABLE to R.color.status_turquesa,
        StatusEnumRobot.STATUS_ROBOT_STABILIZED to R.color.status_blue,
        StatusEnumRobot.STATUS_ROBOT_ERROR to R.color.status_red
    )

    fun mapStatusTostring( statusBt: StatusEnumBT , statusRobot : StatusEnumRobot? ): String {
        if( statusBt == StatusEnumBT.STATUS_CONNECTED && statusRobot != null ){
            return if (mapStatusRobotString.containsKey(statusRobot)) mapStatusRobotString[statusRobot]!! else "XXX"
        }
        else {
            return if (mapStatusBtString.containsKey(statusBt)) mapStatusBtString[statusBt]!! else "XXX"
        }
    }
    fun mapStatusToColor( statusBt: StatusEnumBT , statusRobot : StatusEnumRobot? ): Int {
        if( statusBt == StatusEnumBT.STATUS_CONNECTED ){
            return if (mapStatusRobotColor.containsKey(statusRobot)) mapStatusRobotColor[statusRobot]!! else R.color.gray_80_percent
        }
        else {
            return if (mapStatusBtColor.containsKey(statusBt)) mapStatusBtColor[statusBt]!! else R.color.gray_80_percent
        }
    }
}

enum class StatusEnumBT{
    STATUS_INIT,
    STATUS_DISCOVERING,
    STATUS_CONNECTING,
    STATUS_CONNECTED,
    STATUS_DISCONNECT,
    STATUS_ERROR
}

enum class StatusEnumRobot {
    STATUS_ROBOT_INIT,
    STATUS_ROBOT_DISABLE,
    STATUS_ROBOT_ENABLE,
    STATUS_ROBOT_STABILIZED,
    STATUS_ROBOT_ERROR
}