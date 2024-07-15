package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer
import kotlin.experimental.xor


data class RobotDynamicDataRaw(
    val headerPackage: Short,
    val speedR: Short,
    val speedL: Short,
    val pitchAngle: Short,
    val rollAngle: Short,
    val yawAngle: Short,
    val setPoint: Short,
    val centerAngle: Short,
    val statusCode: Short
)

data class RobotDynamicData(
    val speedR: Int,
    val speedL: Int,
    val pitchAngle: Float,
    val rollAngle: Float,
    val yawAngle: Float,
    val setPoint: Float,
    val centerAngle: Float,
    val statusCode: Int,
)

private val RobotDynamicDataRaw.asRobotDynamicData: RobotDynamicData
    get() = RobotDynamicData(
        speedR.toInt(),
        speedL.toInt(),
        pitchAngle.toFloat() / PRECISION_DECIMALS_COMMS,
        (rollAngle.toFloat() / PRECISION_DECIMALS_COMMS),
        (yawAngle.toFloat() / PRECISION_DECIMALS_COMMS),
        (setPoint.toFloat() / PRECISION_DECIMALS_COMMS),
        (centerAngle.toFloat() / PRECISION_DECIMALS_COMMS),
        statusCode.toInt(),
    )

private val ByteBuffer.asRobotDynamicDataRaw: RobotDynamicDataRaw
    get() = RobotDynamicDataRaw(
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short
    )

val ByteBuffer.asRobotDynamicData: RobotDynamicData
    get() = this.asRobotDynamicDataRaw.asRobotDynamicData