package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer

data class RobotLocalConfigRaw(
    val headerPackage: Short,
    val kp: Short,
    val ki: Short,
    val kd: Short,
    val centerAngle: Short,
    val safetyLimits: Short,
)

data class RobotLocalConfig(
    val kp: Float,
    val ki: Float,
    val kd: Float,
    val centerAngle: Float,
    val safetyLimits: Float,
)

private val RobotLocalConfigRaw.asRobotLocalConfig: RobotLocalConfig
    get() = RobotLocalConfig(
        kp.toFloat() / PRECISION_DECIMALS_COMMS,
        ki.toFloat() / PRECISION_DECIMALS_COMMS,
        kd.toFloat() / PRECISION_DECIMALS_COMMS,
        centerAngle.toFloat() / PRECISION_DECIMALS_COMMS,
        safetyLimits.toFloat() / PRECISION_DECIMALS_COMMS
    )

private val ByteBuffer.asRobotLocalConfigRaw: RobotLocalConfigRaw
    get() = RobotLocalConfigRaw(
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short
    )

val ByteBuffer.asRobotLocalConfig: RobotLocalConfig
    get() = this.asRobotLocalConfigRaw.asRobotLocalConfig