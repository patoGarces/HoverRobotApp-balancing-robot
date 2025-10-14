package com.app.hoverrobot.data.models.comms

enum class CommandsRobot {        // En sync con el codigo del esp!
    CALIBRATE_IMU,
    SAVE_PARAMS_SETTINGS,
    GET_PARAMS_SETTINGS,
    ARMED,
    DEARMED,
    CLEAN_WHEELS,
    PID_ANGLE_TEST,
    MOVE_DISTANCE,      // en m/s
    MOVE_ABS_YAW,
    MOVE_REL_YAW
}

enum class Wheel {
    LEFT_WHEEL,
    RIGHT_WHEEL
}