package com.app.hoverrobot.data.models

data class Battery(
    val isCharging: Boolean = false,
    val level: Int = 0,
    val voltage: Float = 0F
)

fun Float.toPercentLevel(): Int {
    if (this == 0f) return 0
    val batPercent = (((this / 10) - MIN_VOLTAGE_PER_CELL_BATTERY) * 100 /
            (MAX_VOLTAGE_PER_CELL_BATTERY - MIN_VOLTAGE_PER_CELL_BATTERY)).toInt()
    return batPercent.coerceIn(0, 100)
}

const val MIN_VOLTAGE_PER_CELL_BATTERY = 3.7f
const val MAX_VOLTAGE_PER_CELL_BATTERY = 4.2f

const val BATTERY_LEVEL_EMPTY = 10
const val BATTERY_LEVEL_LOW = 25
const val BATTERY_LEVEL_MEDIUM = 60
const val BATTERY_LEVEL_HIGH = 80
