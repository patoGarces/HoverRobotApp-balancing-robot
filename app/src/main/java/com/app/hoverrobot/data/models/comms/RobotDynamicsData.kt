package com.app.hoverrobot.data.models.comms

import com.app.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import com.app.hoverrobot.data.utils.StatusRobot
import java.nio.ByteBuffer
import kotlin.Float


data class FrameRobotDynamicData(
    val robotData: RobotDynamicData,
    val timeStamp: Float,
)

data class CollisionSensors(
    val sensorFrontLeft: Float? = null,
    val sensorFrontRight: Float? = null,
    val sensorRearLeft: Float? = null,
    val sensorRearRight: Float? = null,
)

data class Temperatures(
    val tempImu: Float = 0F,
    val tempMcb: Float = 0F,
    val tempMainboard: Float = 0F,
)

data class RobotDynamicData(
    var isCharging: Boolean,
    val batVoltage: Float,
    val temperatures: Temperatures,
    val speedR: Float,
    val speedL: Float,
    val poswheelR: Float,
    val posWheelL: Float,
    val currentR: Float,
    val currentL: Float,
    val pitchAngle: Float,
    val rollAngle: Float,
    val yawAngle: Float,
    val collisionSensors: CollisionSensors,
    val posInMeters: Float,
    val outputYawControl: Float,
    val setPointAngle: Float,
    val setPointPos: Float,
    val setPointYaw: Float,
    val setPointSpeed: Float,
    val statusCode: StatusRobot,
)

val ByteBuffer.asRobotDynamicData: RobotDynamicData
    get() = RobotDynamicData(
        isCharging = this.short.toInt() != 0,
        batVoltage = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        temperatures = Temperatures(
            tempImu = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
            tempMcb = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
            tempMainboard = this.short.toFloat() / PRECISION_DECIMALS_COMMS
        ),
        speedR = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        speedL = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        poswheelR = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        posWheelL = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        currentR = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        currentL = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        pitchAngle = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        rollAngle = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        yawAngle = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        collisionSensors = CollisionSensors(
            sensorFrontLeft = (this.short.toFloat() / PRECISION_DECIMALS_COMMS).takeIf { it > 0 },
            sensorFrontRight = (this.short.toFloat() / PRECISION_DECIMALS_COMMS).takeIf { it > 0 },
            sensorRearLeft = (this.short.toFloat() / PRECISION_DECIMALS_COMMS).takeIf { it > 0 },
            sensorRearRight = (this.short.toFloat() / PRECISION_DECIMALS_COMMS).takeIf { it > 0 },
        ),
        posInMeters = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        outputYawControl = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        setPointAngle = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        setPointPos = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        setPointYaw = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        setPointSpeed = this.short.toFloat() / PRECISION_DECIMALS_COMMS,
        statusCode = StatusRobot.entries[this.short.toInt()]
    )

const val ROBOT_DYNAMIC_DATA_SIZE = 50  // Total de BYTES sin el headerPackage
