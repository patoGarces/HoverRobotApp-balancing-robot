package com.example.hoverrobot.ui.statusDataFragment

import android.content.Context
import com.example.hoverrobot.R

class MapperGralStatus(context : Context) {

    private val mapStatusGralText = mapOf(
        StatusEnumGral.STATUS_NORMAL.ordinal to context.getString(R.string.status_normal),
        StatusEnumGral.STATUS_INITIALIZING_IMU.ordinal to context.getString(R.string.status_initializing_imu),
        StatusEnumGral.STATUS_ERROR_IMU.ordinal to context.getString(R.string.status_error_imu),
        StatusEnumGral.STATUS_INITIALIZING_MOTORBOARD.ordinal to context.getString(R.string.status_init_motorboard),
        StatusEnumGral.STATUS_ERROR_MOTORBOARD_COMMS.ordinal to context.getString(R.string.status_error_motorboard_comms),
        StatusEnumGral.STATUS_ERROR_MOTORS_HALL.ordinal to context.getString(R.string.status_error_motors_hall),
        StatusEnumGral.STATUS_ERROR_MOTORS_HARDWARE.ordinal to context.getString(R.string.status_error_motors_hardware),
        StatusEnumGral.STATUS_ERROR_MOTORS_BATTERY.ordinal to context.getString(R.string.status_error_motors_battery),
        StatusEnumGral.STATUS_ERROR_CRITICAL.ordinal to context.getString(R.string.status_error_critical),
        StatusEnumGral.STATUS_UNKNOWN.ordinal to context.getString(R.string.status_unknown),
    )

    private val mapStatusGralColor = mapOf(
        StatusEnumGral.STATUS_NORMAL.ordinal to R.color.white,
        StatusEnumGral.STATUS_INITIALIZING_IMU.ordinal to R.color.blue_80_percent,
        StatusEnumGral.STATUS_ERROR_IMU.ordinal to R.color.status_orange,
        StatusEnumGral.STATUS_INITIALIZING_MOTORBOARD.ordinal to R.color.blue_80_percent,
        StatusEnumGral.STATUS_ERROR_MOTORBOARD_COMMS.ordinal to R.color.status_orange,
        StatusEnumGral.STATUS_ERROR_MOTORS_HALL.ordinal to R.color.status_orange,
        StatusEnumGral.STATUS_ERROR_MOTORS_HARDWARE.ordinal to R.color.status_orange,
        StatusEnumGral.STATUS_ERROR_MOTORS_BATTERY.ordinal to R.color.yellow_80_percent,
        StatusEnumGral.STATUS_ERROR_CRITICAL.ordinal to R.color.red_80_percent,
        StatusEnumGral.STATUS_UNKNOWN.ordinal to R.color.status_orange,
    )

    fun mapGralStatusText(status: Int): String {
        return if (mapStatusGralText.containsKey(status)) mapStatusGralText[status]!! else mapStatusGralText[StatusEnumGral.STATUS_UNKNOWN.ordinal]!!
    }
    fun mapGralStatusToColor( status: Int): Int {
        return if (mapStatusGralColor.containsKey(status)) mapStatusGralColor[status]!! else R.color.gray_80_percent
    }
}

enum class StatusEnumGral{

    STATUS_NORMAL,
    STATUS_INITIALIZING_IMU,
    STATUS_ERROR_IMU,
    STATUS_INITIALIZING_MOTORBOARD,
    STATUS_ERROR_MOTORBOARD_COMMS,
    STATUS_ERROR_MOTORS_HALL,
    STATUS_ERROR_MOTORS_HARDWARE,
    STATUS_ERROR_MOTORS_BATTERY,
    STATUS_ERROR_CRITICAL,
    STATUS_UNKNOWN,
}
