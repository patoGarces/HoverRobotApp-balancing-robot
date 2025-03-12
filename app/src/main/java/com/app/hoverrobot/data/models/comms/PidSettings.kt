package com.app.hoverrobot.data.models.comms

data class PidSettings(
    val indexPid: Int,
    val kp : Float,
    val ki : Float,
    val kd : Float,
    val centerAngle : Float,
    val safetyLimits : Float
)