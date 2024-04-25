package com.example.hoverrobot.data.models.comms

import java.nio.ByteBuffer

data class MainBoardRobotStatus(
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
    val pid: PidSettings,
    val setPoint: Float,
    val ordenCode: Short,
    val statusCode: Short,
    val checksum: Short
)

val ByteBuffer.asRobotStatus: MainBoardRobotStatus
    get() = MainBoardRobotStatus (
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.short,
        this.float,
        this.float,
        this.float,
        PidSettings(
            this.float,
            this.float,
            this.float,
            this.float,
            this.float,
        ),
        this.float,
        this.short,
        this.short,
        this.short
    )