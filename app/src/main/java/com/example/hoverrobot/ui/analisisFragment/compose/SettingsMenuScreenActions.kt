package com.example.hoverrobot.ui.analisisFragment.compose

import com.example.hoverrobot.ui.analisisFragment.resources.SelectedDataset

sealed class SettingsMenuActions {
    data class OnDatasetChange(val selectedDataset: SelectedDataset?): SettingsMenuActions()
    data class OnAutoScaleChange(val isEnable: Boolean): SettingsMenuActions()
    data class OnPauseChange(val isPaused: Boolean): SettingsMenuActions()
    data object OnClearData: SettingsMenuActions()

}