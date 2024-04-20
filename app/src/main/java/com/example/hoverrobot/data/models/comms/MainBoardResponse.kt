package com.example.hoverrobot.data.models.comms

import kotlinx.serialization.Serializable

@Serializable
data class MainBoardResponse(
    val header: Short,
    val batVoltage: Short,
    val batPercent: Short,
    val batTemp : Short,
    val tempUcControl: Short,
    val tempUcMain: Short,
    val speedR: Short,
    val speedL: Short,
    val pitchAngle: Float,
    val rollAngle: Float,
    val yawAngle: Float,
    val kp: Float,
    val ki: Float,
    val kd: Float,
    val centerAngle: Float,
    val safetyLimits: Float,
    val ordenCode: Short,
    val statusCode: Short,
    val checksum: Short
)