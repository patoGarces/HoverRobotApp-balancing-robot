package com.example.hoverrobot.data.models

data class Battery(
    val isCharging: Boolean,
    val batLevel : Int,
    val batVoltage : Float
)
