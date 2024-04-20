package com.example.hoverrobot.data.models.comms

@kotlinx.serialization.Serializable
data class PidSettings(
    val kp : Float,
    val ki : Float,
    val kd : Float,
    val centerAngle : Float,
    val safetyLimits : Float
)