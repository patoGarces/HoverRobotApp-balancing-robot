package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer
import kotlin.experimental.xor


data class RobotDynamicData(
    val batVoltage: Float,
    val tempImu: Float,
    val speedR: Int,
    val speedL: Int,
    val pitchAngle: Float,
    val rollAngle: Float,
    val yawAngle: Float,
    val setPoint: Float,
    val centerAngle: Float,
    val statusCode: Int,
)

val ByteBuffer.asRobotDynamicData: RobotDynamicData
    get() = RobotDynamicData(
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toInt(),
        this.short.toInt(),
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toInt()
    )

const val ROBOT_DYNAMIC_DATA_SIZE = 20
