package com.example.hoverrobot.data.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.hoverrobot.R

object StatusMapper {
    private val mapStatusConnectionString = mapOf(
        StatusConnection.INIT to R.string.status_connection_init,
        StatusConnection.WAITING to R.string.status_connection_waiting,
        StatusConnection.CONNECTED to R.string.status_connection_connected,
        StatusConnection.ERROR to R.string.status_connection_error
    )

    private val mapStatusConnectionColor = mapOf(
        StatusConnection.INIT to R.color.status_orange,
        StatusConnection.WAITING to R.color.status_turquesa,
        StatusConnection.CONNECTED to R.color.status_blue,
        StatusConnection.ERROR to R.color.status_red
    )

    private val mapStatusRobotString = mapOf(
        StatusRobot.INIT to R.string.status_robot_init,
        StatusRobot.DISABLE to R.string.status_robot_disable,
        StatusRobot.ARMED to R.string.status_robot_enable,
        StatusRobot.STABILIZED to R.string.status_robot_stabilized,
        StatusRobot.CHARGING to R.string.status_robot_charging,
        StatusRobot.TEST_MODE to R.string.status_robot_test_mode,
        StatusRobot.ERROR_MCB to R.string.status_robot_error_mcb,
        StatusRobot.ERROR to R.string.status_robot_error,
    )

    private val mapStatusRobotColor = mapOf(
        StatusRobot.INIT to R.color.status_orange,
        StatusRobot.DISABLE to R.color.gray_80_percent,
        StatusRobot.ARMED to R.color.status_turquesa,
        StatusRobot.STABILIZED to R.color.status_blue,
        StatusRobot.CHARGING to R.color.status_orange,
        StatusRobot.TEST_MODE to R.color.status_orange,
        StatusRobot.ERROR to R.color.status_red,
        StatusRobot.ERROR_MCB to R.color.status_red,
        StatusRobot.ERROR_IMU to R.color.status_red,
        StatusRobot.ERROR_HALLS to R.color.status_red,
        StatusRobot.ERROR_BATTERY to R.color.status_red,
        StatusRobot.ERROR_LIMIT_SPEED to R.color.status_red,
        StatusRobot.ERROR_LIMIT_ANGLE to R.color.status_red,
    )

    fun StatusConnection.stringRes(): Int {
        return mapStatusConnectionString[this] ?: R.string.status_connection_error
    }

    fun StatusConnection.colorRes(): Int {
        return mapStatusConnectionColor[this] ?: R.color.gray_80_percent
    }

    fun StatusRobot.stringRes(statusConnection: StatusConnection): Int {
        return if (statusConnection != StatusConnection.CONNECTED) mapStatusConnectionString[StatusConnection.WAITING]!!
        else mapStatusRobotString[this] ?: R.string.status_robot_error
    }

    fun StatusRobot.colorRes(statusConnection: StatusConnection): Int {
        return if (statusConnection != StatusConnection.CONNECTED) mapStatusConnectionColor[StatusConnection.WAITING]!!
        else mapStatusRobotColor[this] ?: R.color.gray_80_percent
    }

    fun StatusRobot.stringBtnStatusRes(statusConnection: StatusConnection): Int {
        return if (statusConnection != StatusConnection.CONNECTED) mapStatusConnectionString[statusConnection]!!
        else mapStatusRobotString[this] ?: R.string.status_robot_error
    }

    fun StatusRobot.colorBtnStatusRes(statusConnection: StatusConnection): Int {
        return if (statusConnection != StatusConnection.CONNECTED) mapStatusConnectionColor[statusConnection]!!
        else mapStatusRobotColor[this] ?: R.color.gray_80_percent
    }

    fun StatusRobot.colorStatusLog(): Int {
        return  mapStatusRobotColor[this] ?: R.color.gray_80_percent
    }

    fun Context.colorFromRes(@ColorRes colorRes: Int): Color {
        return Color(ContextCompat.getColor(this, colorRes))
    }
}

enum class StatusConnection {
    INIT,
    WAITING,
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
    ERROR_MCB,
    ERROR_IMU,
    ERROR_HALLS,
    ERROR_BATTERY,
    TEST_MODE;
}