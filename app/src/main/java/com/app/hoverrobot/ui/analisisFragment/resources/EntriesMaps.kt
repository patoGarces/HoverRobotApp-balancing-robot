package com.app.hoverrobot.ui.analisisFragment.resources

import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.FrameRobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.toPercentLevel
import com.github.mikephil.charting.data.Entry

object EntriesMaps {

    val datasetLabels = mapOf(
        LineDataKeys.LINEDATA_KEY_ANGLE_PITCH to R.string.dataset_angle_pitch,
        LineDataKeys.LINEDATA_KEY_ANGLE_ROLL to R.string.dataset_angle_roll,
        LineDataKeys.LINEDATA_KEY_ANGLE_YAW to R.string.dataset_angle_yaw,
        LineDataKeys.LINEDATA_KEY_SPEED_L to R.string.dataset_speed_motor_l,
        LineDataKeys.LINEDATA_KEY_SPEED_R to R.string.dataset_speed_motor_r,
        LineDataKeys.LINEDATA_KEY_CURRENT_L to R.string.dataset_current_motor_l,
        LineDataKeys.LINEDATA_KEY_CURRENT_R to R.string.dataset_current_motor_r,
        LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE to R.string.dataset_set_point_angle,
        LineDataKeys.LINEDATA_KEY_SETPOINT_POS to R.string.dataset_set_point_pos,
        LineDataKeys.LINEDATA_KEY_SETPOINT_YAW to R.string.dataset_set_point_yaw,
        LineDataKeys.LINEDATA_KEY_SETPOINT_SPEED to R.string.dataset_set_point_speed,
        LineDataKeys.LINEDATA_KEY_OUTPUT_YAW to R.string.dataset_output_yaw,
        LineDataKeys.LINEDATA_KEY_POS_IN_MTS to R.string.dataset_position_meters,
        LineDataKeys.LINEDATA_KEY_ACTUAL_SPEED to R.string.dataset_speed,
        LineDataKeys.LINEDATA_KEY_BATTERY_LVL to R.string.dataset_battery_level
    )

    val datasetColors = mapOf(
        LineDataKeys.LINEDATA_KEY_ANGLE_PITCH to R.color.red_80_percent,
        LineDataKeys.LINEDATA_KEY_ANGLE_ROLL to R.color.green_80_percent,
        LineDataKeys.LINEDATA_KEY_ANGLE_YAW to R.color.yellow_80_percent,
        LineDataKeys.LINEDATA_KEY_SPEED_L to R.color.blue_80_percent,
        LineDataKeys.LINEDATA_KEY_SPEED_R to R.color.red_80_percent,
        LineDataKeys.LINEDATA_KEY_CURRENT_L to R.color.status_orange,
        LineDataKeys.LINEDATA_KEY_CURRENT_R to R.color.status_green,
        LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE to R.color.status_turquesa,
        LineDataKeys.LINEDATA_KEY_SETPOINT_POS to R.color.status_green,
        LineDataKeys.LINEDATA_KEY_SETPOINT_YAW to R.color.status_turquesa,
        LineDataKeys.LINEDATA_KEY_SETPOINT_SPEED to R.color.blue_80_percent,
        LineDataKeys.LINEDATA_KEY_OUTPUT_YAW to R.color.red_80_percent,
        LineDataKeys.LINEDATA_KEY_POS_IN_MTS to R.color.red_80_percent,
        LineDataKeys.LINEDATA_KEY_ACTUAL_SPEED to R.color.red_80_percent,
        LineDataKeys.LINEDATA_KEY_BATTERY_LVL to R.color.status_turquesa
    )

    fun MutableMap<LineDataKeys, MutableList<Entry>>.updateWithFrame(newFrame: FrameRobotDynamicData) {
        this[LineDataKeys.LINEDATA_KEY_ANGLE_PITCH]?.add(Entry(newFrame.timeStamp, newFrame.robotData.pitchAngle))
        this[LineDataKeys.LINEDATA_KEY_ANGLE_ROLL]?.add(Entry(newFrame.timeStamp, newFrame.robotData.rollAngle))
        this[LineDataKeys.LINEDATA_KEY_ANGLE_YAW]?.add(Entry(newFrame.timeStamp, newFrame.robotData.yawAngle))
        this[LineDataKeys.LINEDATA_KEY_SPEED_L]?.add(Entry(newFrame.timeStamp, newFrame.robotData.speedL.toFloat() / 10))
        this[LineDataKeys.LINEDATA_KEY_SPEED_R]?.add(Entry(newFrame.timeStamp, newFrame.robotData.speedR.toFloat() / 10))
        this[LineDataKeys.LINEDATA_KEY_CURRENT_L]?.add(Entry(newFrame.timeStamp, newFrame.robotData.currentL))
        this[LineDataKeys.LINEDATA_KEY_CURRENT_R]?.add(Entry(newFrame.timeStamp, newFrame.robotData.currentR))
        this[LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE]?.add(Entry(newFrame.timeStamp, newFrame.robotData.setPointAngle))
        this[LineDataKeys.LINEDATA_KEY_SETPOINT_POS]?.add(Entry(newFrame.timeStamp, newFrame.robotData.setPointPos))
        this[LineDataKeys.LINEDATA_KEY_SETPOINT_YAW]?.add(Entry(newFrame.timeStamp, newFrame.robotData.setPointYaw))
        this[LineDataKeys.LINEDATA_KEY_SETPOINT_SPEED]?.add(Entry(newFrame.timeStamp, newFrame.robotData.setPointSpeed))
        this[LineDataKeys.LINEDATA_KEY_POS_IN_MTS]?.add(Entry(newFrame.timeStamp, newFrame.robotData.posInMeters))
        this[LineDataKeys.LINEDATA_KEY_OUTPUT_YAW]?.add(Entry(newFrame.timeStamp, newFrame.robotData.outputYawControl))
        this[LineDataKeys.LINEDATA_KEY_ACTUAL_SPEED]?.add(Entry(newFrame.timeStamp, newFrame.robotData.speedL.toFloat()))  // TODO: tomar velocidad promedio
        this[LineDataKeys.LINEDATA_KEY_BATTERY_LVL]?.add(Entry(newFrame.timeStamp, newFrame.robotData.batVoltage.toPercentLevel().toFloat()))
    }
}

enum class LineDataKeys {
    LINEDATA_KEY_ANGLE_PITCH,
    LINEDATA_KEY_ANGLE_ROLL,
    LINEDATA_KEY_ANGLE_YAW,
    LINEDATA_KEY_SPEED_L,
    LINEDATA_KEY_SPEED_R,
    LINEDATA_KEY_CURRENT_L,
    LINEDATA_KEY_CURRENT_R,
    LINEDATA_KEY_SETPOINT_ANGLE,
    LINEDATA_KEY_SETPOINT_POS,
    LINEDATA_KEY_SETPOINT_YAW,
    LINEDATA_KEY_SETPOINT_SPEED,
    LINEDATA_KEY_OUTPUT_YAW,
    LINEDATA_KEY_POS_IN_MTS,
    LINEDATA_KEY_ACTUAL_SPEED,
    LINEDATA_KEY_BATTERY_LVL
}

enum class SelectedDataset {
    DATASET_IMU,
    DATASET_POWER,
    DATASET_PID_ANGLE,
    DATASET_PID_POS,
    DATASET_PID_YAW,
    DATASET_PID_SPEED
}