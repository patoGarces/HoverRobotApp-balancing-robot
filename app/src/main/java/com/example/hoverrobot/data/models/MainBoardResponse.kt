package com.example.hoverrobot.Models.comms

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
    val centerAngle: Float,
    val safetyLimits: Short,
    val kp: Short,
    val ki: Short,
    val kd: Short,
    val ordenCode: Short,
    val statusCode: Short,
    val checksum: Short
)