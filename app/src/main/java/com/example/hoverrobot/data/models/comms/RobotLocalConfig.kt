package com.example.hoverrobot.data.models.comms

import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer

data class RobotLocalConfig(
    val centerAngle: Float,
    val safetyLimits: Float,
    val pids: List<PidParams>
)

val ByteBuffer.asRobotLocalConfig: RobotLocalConfig
    get() {
        val centerAngle = this.short.toFloat() / PRECISION_DECIMALS_COMMS
        val safetyLimits = this.short.toFloat() / PRECISION_DECIMALS_COMMS

        val pidParams = mutableListOf<PidParams>()
        for (i in 0 until CANT_PIDS) {
            val kp = short.toFloat() / PRECISION_DECIMALS_COMMS
            val ki = short.toFloat() / PRECISION_DECIMALS_COMMS
            val kd = short.toFloat() / PRECISION_DECIMALS_COMMS
            pidParams.add(PidParams(kp, ki, kd))
        }
        return RobotLocalConfig(centerAngle, safetyLimits, pidParams)
    }


const val CANT_PIDS = 4                     // OJO: en sync con el firmware
const val ROBOT_LOCAL_CONFIG_SIZE = 28//22