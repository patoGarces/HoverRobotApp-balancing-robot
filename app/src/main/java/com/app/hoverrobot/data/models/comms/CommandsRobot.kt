package com.app.hoverrobot.data.models.comms

enum class CommandsRobot {        // En sync con el codigo del esp!
    CALIBRATE_IMU,
    SAVE_PARAMS_SETTINGS,
    ARMED,
    DEARMED,
    CLEAN_WHEELS,
    VIBRATION_TEST,
    MOVE_FORWARD,
    MOVE_BACKWARD,
    MOVE_ABS_YAW,
    MOVE_REL_YAW
}

enum class Wheel {
    LEFT_WHEEL,
    RIGHT_WHEEL
}