package com.example.hoverrobot.ui.settingsFragment.compose

import com.example.hoverrobot.data.models.comms.RobotLocalConfig

sealed class OnActionSettingsScreen {
    data object OnCalibrateImu: OnActionSettingsScreen()
    data object OnCleanLeftMotor: OnActionSettingsScreen()
    data object OnCleanRightMotor: OnActionSettingsScreen()
    data class OnNewSettings(val newRobotLocalConfig: RobotLocalConfig): OnActionSettingsScreen()
}