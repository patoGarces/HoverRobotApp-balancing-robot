package com.app.hoverrobot.ui.settingsFragment.compose

import com.app.hoverrobot.data.models.comms.PidSettings

sealed class OnActionSettingsScreen {
    data object OnCalibrateImu: OnActionSettingsScreen()
    data object OnCleanLeftMotor: OnActionSettingsScreen()
    data object OnCleanRightMotor: OnActionSettingsScreen()
    data class OnNewSettings(val pidSettings: PidSettings): OnActionSettingsScreen()
}