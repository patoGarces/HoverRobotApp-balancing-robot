package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer
import kotlin.experimental.xor


data class RobotDynamicDataRaw(
    val header: Short,
    val speedR: Short,
    val speedL: Short,
    val pitchAngle: Short,
    val rollAngle: Short,
    val yawAngle: Short,
    val setPoint: Short,
    val centerAngle: Short,
    val statusCode: Short,
    val checksum: Short
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

val RobotDynamicDataRaw.asRobotDynamicData: RobotDynamicData
    get() = RobotDynamicData(
        speedR.toInt(),
        speedL.toInt(),
        (pitchAngle / PRECISION_DECIMALS_COMMS).toFloat(),
        (rollAngle / PRECISION_DECIMALS_COMMS).toFloat(),
        (yawAngle / PRECISION_DECIMALS_COMMS).toFloat(),
        (setPoint / PRECISION_DECIMALS_COMMS).toFloat(),
        (centerAngle / PRECISION_DECIMALS_COMMS).toFloat(),
        statusCode.toInt(),
    )

val ByteBuffer.asRobotDynamicDataRaw: RobotDynamicDataRaw
    get() = RobotDynamicDataRaw(
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
    )

val RobotDynamicDataRaw.calculateChecksum: Short
    get() = (
            header xor
            speedR xor
            speedL xor
            pitchAngle xor
            rollAngle xor
            yawAngle xor
            setPoint xor
            centerAngle xor
            statusCode
        )
