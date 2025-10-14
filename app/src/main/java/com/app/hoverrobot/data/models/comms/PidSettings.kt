package com.app.hoverrobot.data.models.comms

data class PidSettings(
    val indexPid: Int,
    val kp : Float,
    val ki : Float,
    val kd : Float,
    val safetyLimits : Float
)

enum class PidIndexSetting {        // OJO: en sync con el esp32
    PID_ANGLE,
    PID_POS,
    PID_SPEED,
    PID_YAW
}