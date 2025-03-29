package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import com.app.hoverrobot.data.utils.StatusRobot
import java.nio.ByteBuffer


data class FrameRobotDynamicData(
    val robotData: RobotDynamicData,
    val timeStamp: Float,
)

data class RobotDynamicData(
    var isCharging: Boolean,
    val batVoltage: Float,
    val tempImu: Float,
    val tempMcb: Float,
    val tempMainboard: Float,
    val speedR: Int,
    val speedL: Int,
    val currentR: Float,
    val currentL: Float,
    val pitchAngle: Float,
    val rollAngle: Float,
    val yawAngle: Float,
    val posInMeters: Float,
    val outputYawControl: Float,
    val setPointAngle: Float,
    val setPointPos: Float,
    val setPointYaw: Float,
    val setPointSpeed: Float,
    val centerAngle: Float,
    val statusCode: StatusRobot,
)

val ByteBuffer.asRobotDynamicData: RobotDynamicData
    get() = RobotDynamicData(
        this.short.toInt() != 0,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toInt(),
        this.short.toInt(),
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        StatusRobot.entries[this.short.toInt()]
    )

const val ROBOT_DYNAMIC_DATA_SIZE = 40
