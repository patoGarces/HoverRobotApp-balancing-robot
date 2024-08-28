package com.example.hoverrobot.data.models.comms

data class DirectionControl(
    val joyAxisX: Short,
    val joyAxisY: Short,
    val compassYaw: Short
)
