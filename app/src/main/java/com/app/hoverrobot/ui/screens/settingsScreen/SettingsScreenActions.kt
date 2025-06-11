package com.app.hoverrobot.ui.screens.settingsScreen

import com.app.hoverrobot.data.models.comms.PidSettings

sealed class SettingsScreenActions {
    data object OnCalibrateImu: SettingsScreenActions()
    data object OnCleanLeftMotor: SettingsScreenActions()
    data object OnCleanRightMotor: SettingsScreenActions()
    data class OnNewSettings(val pidSettings: PidSettings): SettingsScreenActions()
    data class OnReconnectToRobot(val lastIntIp: Int): SettingsScreenActions()
    data class OnReconnectToRaspi(val lastIntIp: Int): SettingsScreenActions()
}