package com.example.hoverrobot.data.utils

import android.content.Context
import com.example.hoverrobot.R

class MapperGralStatus(context: Context) {

    private val mapStatusGralText = mapOf(
        StatusEnumGral.NORMAL to context.getString(R.string.status_normal),
        StatusEnumGral.INITIALIZING_IMU to context.getString(R.string.status_initializing_imu),
        StatusEnumGral.ERROR_IMU to context.getString(R.string.status_error_imu),
        StatusEnumGral.INITIALIZING_MOTORBOARD to context.getString(R.string.status_init_motorboard),
        StatusEnumGral.ERROR_MCB to context.getString(R.string.status_error_motorboard_comms),
        StatusEnumGral.ERROR_MOTORS_HALL to context.getString(R.string.status_error_motors_hall),
        StatusEnumGral.ERROR_MOTORS_HARDWARE to context.getString(R.string.status_error_motors_hardware),
        StatusEnumGral.ERROR_MOTORS_BATTERY to context.getString(R.string.status_error_motors_battery),
        StatusEnumGral.ERROR_CRITICAL to context.getString(R.string.status_error_critical),
        StatusEnumGral.UNKNOWN to context.getString(R.string.status_unknown),
    )

    private val mapStatusGralColor = mapOf(
        StatusEnumGral.NORMAL to R.color.white,
        StatusEnumGral.INITIALIZING_IMU to R.color.blue_80_percent,
        StatusEnumGral.ERROR_IMU to R.color.status_orange,
        StatusEnumGral.INITIALIZING_MOTORBOARD to R.color.blue_80_percent,
        StatusEnumGral.ERROR_MCB to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_HALL to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_HARDWARE to R.color.status_orange,
        StatusEnumGral.ERROR_MOTORS_BATTERY to R.color.yellow_80_percent,
        StatusEnumGral.ERROR_CRITICAL to R.color.red_80_percent,
        StatusEnumGral.UNKNOWN to R.color.status_orange,
    )

    fun mapGralStatusText(status: StatusEnumGral): String {
        return if (mapStatusGralText.containsKey(status)) mapStatusGralText[status]!! else mapStatusGralText[StatusEnumGral.UNKNOWN]!!
    }

    fun mapGralStatusToColor(status: StatusEnumGral): Int {
        return if (mapStatusGralColor.containsKey(status)) mapStatusGralColor[status]!! else R.color.gray_80_percent
    }
}

enum class StatusEnumGral {
    NORMAL,
    INITIALIZING_IMU,
    ERROR_IMU,
    INITIALIZING_MOTORBOARD,
    ERROR_MCB,
    ERROR_MOTORS_HALL,
    ERROR_MOTORS_HARDWARE,
    ERROR_MOTORS_BATTERY,
    ERROR_CRITICAL,
    DISCONNECTED,
    UNKNOWN,
}
