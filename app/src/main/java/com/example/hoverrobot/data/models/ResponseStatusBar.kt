package com.example.hoverrobot.Models.comms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseStatusBar(
    val imu_temp : Float,
    val battery : Battery,
)

@Serializable
data class Battery(
    @SerialName("bat_level")
    val batLevel : Int,
    @SerialName("bat_voltage")
    val batVoltage : Float,
    @SerialName("bat_temp")
    val batTemp : Float,
)
