package com.example.hoverrobot.data.models.comms

import java.nio.ByteBuffer
import kotlin.experimental.xor


//TODO: eliminar
//data class MainBoardRobotStatus(
//    val header: Short,
//    val batVoltage: Short,
//    val batPercent: Short,
//    val batTemp: Short,
//    val tempImu: Short,
//    val tempEsc: Short,
//    val speedR: Short,
//    val speedL: Short,
//    val pitchAngle: Float,
//    val rollAngle: Float,
//    val yawAngle: Float,
//    val pid: PidSettings,
//    val setPoint: Float,
//    val ordenCode: Short,
//    val statusCode: Short,
//    val checksum: Short
//)
//
//val ByteBuffer.asRobotStatus: MainBoardRobotStatus
//    get() = MainBoardRobotStatus(
//        this.short,
//        this.short,
//        this.short,
//        this.short,
//        this.short,
//        this.short,
//        this.short,
//        this.short,
//        this.float,
//        this.float,
//        this.float,
//        PidSettings(
//            this.float,
//            this.float,
//            this.float,
//            this.float,
//            this.float,
//        ),
//        this.float,
//        this.short,
//        this.short,
//        this.short
//    )
//
//val MainBoardRobotStatus.calculateChecksum: Short
//    get() = (
//            header xor
//            batVoltage xor
//            batPercent xor
//            batTemp xor
//            tempImu xor
//            tempEsc xor
//            speedR xor
//            speedL xor
//            pitchAngle.toInt().toShort() xor
//            rollAngle.toInt().toShort() xor
//            yawAngle.toInt().toShort() xor
//            pid.kp.toInt().toShort() xor
//            pid.ki.toInt().toShort() xor
//            pid.kd.toInt().toShort() xor
//            pid.centerAngle.toInt().toShort() xor
//            pid.safetyLimits.toInt().toShort() xor
//            setPoint.toInt().toShort() xor
//            ordenCode xor
//            statusCode
//        )