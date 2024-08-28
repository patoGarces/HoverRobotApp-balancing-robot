package com.example.hoverrobot.data.models.comms

enum class CommandsRobot {        // En sync con el codigo del esp!
    CALIBRATE_IMU,
    SAVE_PARAMS_SETTINGS,
    ARMED_ROBOT,
    DISARMED_ROBOT,
    VIBRATION_TEST,
    COMMAND_MOVE_FORWARD,
    COMMAND_MOVE_BACKWARD
}