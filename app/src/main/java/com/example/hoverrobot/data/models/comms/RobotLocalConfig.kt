package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer

data class RobotLocalConfig(
    val kp: Float,
    val ki: Float,
    val kd: Float,
    val centerAngle: Float,
    val safetyLimits: Float,
)

val ByteBuffer.asRobotLocalConfig: RobotLocalConfig
    get() = RobotLocalConfig(
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS
    )