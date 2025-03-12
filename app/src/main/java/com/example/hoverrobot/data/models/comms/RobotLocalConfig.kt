package com.example.hoverrobot.data.models.comms

import android.util.Log
import com.example.hoverrobot.data.repositories.PRECISION_DECIMALS_COMMS
import java.nio.ByteBuffer

data class RobotLocalConfig(
    val centerAngle: Float = 0F,
    val safetyLimits: Float = 0F,
    val pids: List<PidParams> = listOf(
        PidParams(0f, 0f, 0f),
        PidParams(0f, 0f, 0f),
        PidParams(0f, 0f, 0f),
        PidParams(0f, 0f, 0f),
    )
)

const val CANT_PIDS = 4                     // OJO: en sync con el firmware
const val ROBOT_LOCAL_CONFIG_SIZE = 28

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

fun RobotLocalConfig.asPidSettings(indexPid: Int): PidSettings {
    return PidSettings(
        indexPid = indexPid,
        kp = pids[indexPid].kp,
        ki = pids[indexPid].ki,
        kd = pids[indexPid].kd,
        centerAngle = centerAngle,
        safetyLimits = safetyLimits,
    )
}

fun PidSettings.isDiffWithOriginalLocalConfig(originalLocalConfig: RobotLocalConfig): Boolean =
    this.kp != originalLocalConfig.pids[this.indexPid].kp ||
    this.ki != originalLocalConfig.pids[this.indexPid].ki ||
    this.kd != originalLocalConfig.pids[this.indexPid].kd ||
    this.centerAngle != originalLocalConfig.centerAngle ||
    this.safetyLimits != originalLocalConfig.safetyLimits