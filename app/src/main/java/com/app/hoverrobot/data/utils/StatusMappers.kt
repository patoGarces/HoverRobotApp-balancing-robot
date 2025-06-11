package com.app.hoverrobot.data.utils

import androidx.compose.ui.graphics.Color
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.composeUtils.CustomColors

object StatusMapper {
    fun StatusConnection.toStringRes(): Int =
        when (this) {
            StatusConnection.DISCONNECTED -> R.string.status_connection_disconnected
            StatusConnection.WAITING -> R.string.status_connection_waiting
            StatusConnection.SEARCHING -> R.string.status_connection_searching
            StatusConnection.CONNECTED -> R.string.status_connection_connected
            StatusConnection.ERROR -> R.string.status_connection_error
        }

    fun StatusRobot.toStringRes(): Int =
        when (this) {
            StatusRobot.INIT -> R.string.status_robot_init
            StatusRobot.DISABLE -> R.string.status_robot_disable
            StatusRobot.ARMED -> R.string.status_robot_enable
            StatusRobot.STABILIZED -> R.string.status_robot_stabilized
            StatusRobot.CHARGING -> R.string.status_robot_charging
            StatusRobot.TEST_MODE -> R.string.status_robot_test_mode
            StatusRobot.ERROR_BATTERY -> R.string.status_robot_error_battery
            StatusRobot.ERROR_IMU -> R.string.status_robot_error_imu
            StatusRobot.ERROR_HALL_L -> R.string.status_robot_error_hall_r
            StatusRobot.ERROR_HALL_R -> R.string.status_robot_error_hall_r
            StatusRobot.ERROR_MCB_CONNECTION -> R.string.status_robot_error_mcb
            else -> R.string.status_robot_error
        }

    fun StatusRobot.toStringRes(statusConnection: StatusConnection): Int {
        return if (statusConnection != StatusConnection.CONNECTED) statusConnection.toStringRes()
        else this.toStringRes()
    }

    fun StatusRobot.toColor(statusConnection: StatusConnection): Color =
        if (statusConnection != StatusConnection.CONNECTED) statusConnection.toColor()
        else this.toColor()

    fun StatusRobot.toColor(): Color =
        when (this) {
            StatusRobot.INIT -> CustomColors.StatusOrange
            StatusRobot.DISABLE -> Color.Gray
            StatusRobot.ARMED -> CustomColors.StatusTurquesa
            StatusRobot.STABILIZED -> CustomColors.StatusBlue
            StatusRobot.CHARGING -> CustomColors.StatusOrange
            StatusRobot.TEST_MODE -> CustomColors.StatusOrange
            else -> CustomColors.StatusRed
        }

    fun StatusConnection.toColor(): Color =
        when (this) {
            StatusConnection.DISCONNECTED -> CustomColors.StatusOrange
            StatusConnection.WAITING -> CustomColors.StatusTurquesa
            StatusConnection.SEARCHING -> CustomColors.StatusTurquesa
            StatusConnection.CONNECTED -> CustomColors.StatusBlue
            StatusConnection.ERROR -> CustomColors.StatusRed
        }
}

enum class StatusConnection {
    DISCONNECTED,
    WAITING,
    SEARCHING,
    CONNECTED,
    ERROR
}

enum class StatusRobot {
    INIT,
    DISABLE,
    ARMED,
    STABILIZED,
    CHARGING,
    ERROR,
    ERROR_LIMIT_SPEED,
    ERROR_LIMIT_ANGLE,
    ERROR_MCB_CONNECTION,
    ERROR_IMU,
    ERROR_HALL_L,
    ERROR_HALL_R,
    ERROR_TEMP,
    ERROR_BATTERY,
    TEST_MODE;
}