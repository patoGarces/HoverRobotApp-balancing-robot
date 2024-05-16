package com.example.hoverrobot.data.utils

import android.content.Context
import com.example.hoverrobot.R

class MapperGralStatus(context: Context) {

    private val mapStatusGralText = mapOf(
        StatusEnumGral.NORMAL.ordinal to context.getString(R.string.status_normal),
        StatusEnumGral.INITIALIZING_IMU.ordinal to context.getString(R.string.status_initializing_imu),
        StatusEnumGral.ERROR_IMU.ordinal to context.getString(R.string.status_error_imu),
        StatusEnumGral.INITIALIZING_MOTORBOARD.ordinal to context.getString(R.string.status_init_motorboard),
        StatusEnumGral.ERROR_MOTORBOARD_COMMS.ordinal to context.getString(R.string.status_error_motorboard_comms),
        StatusEnumGral.ERROR_MOTORS_HALL.ordinal to context.getString(R.string.status_error_motors_hall),
        StatusEnumGral.ERROR_MOTORS_HARDWARE.ordinal to context.getString(R.string.status_error_motors_hardware),
        StatusEnumGral.ERROR_MOTORS_BATTERY.ordinal to context.getString(R.string.status_error_motors_battery),
        StatusEnumGral.ERROR_CRITICAL.ordinal to context.getString(R.string.status_error_critical),
        StatusEnumGral.UNKNOWN.ordinal to context.getString(R.string.status_unknown),
    )

    private val mapStatusGralColor = mapOf(
        StatusEnumGral.NORMAL.ordinal to R.color.white,
        StatusEnumGral.INITIALIZING_IMU.ordinal to R.color.blue_80_percent,
        StatusEnumGral.ERROR_IMU.ordinal to R.color.status_orange,
        StatusEnumGral.INITIALIZING_MOTORBOARD.ordinal to R.color.blue_80_percent,
        StatusEnumGral.ERROR_MOTORBOARD_COMMS.ordinal to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_HALL.ordinal to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_HARDWARE.ordinal to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_BATTERY.ordinal to R.color.yellow_80_percent,
        StatusEnumGral.ERROR_CRITICAL.ordinal to R.color.red_80_percent,
        StatusEnumGral.UNKNOWN.ordinal to R.color.status_orange,
    )

    fun mapGralStatusText(status: Int): String {
        return if (mapStatusGralText.containsKey(status)) mapStatusGralText[status]!! else mapStatusGralText[StatusEnumGral.UNKNOWN.ordinal]!!
    }

    fun mapGralStatusToColor(status: Int): Int {
        return if (mapStatusGralColor.containsKey(status)) mapStatusGralColor[status]!! else R.color.gray_80_percent
    }
}

enum class StatusEnumGral {
    NORMAL,
    INITIALIZING_IMU,
    ERROR_IMU,
    INITIALIZING_MOTORBOARD,
    ERROR_MOTORBOARD_COMMS,
    ERROR_MOTORS_HALL,
    ERROR_MOTORS_HARDWARE,
    ERROR_MOTORS_BATTERY,
    ERROR_CRITICAL,
    UNKNOWN,
}
