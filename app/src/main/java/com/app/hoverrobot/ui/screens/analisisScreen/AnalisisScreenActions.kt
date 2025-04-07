package com.app.hoverrobot.ui.screens.analisisScreen

import com.app.hoverrobot.ui.screens.analisisScreen.resources.SelectedDataset

sealed class AnalisisScreenActions {
    data class OnDatasetChange(val selectedDataset: SelectedDataset?): AnalisisScreenActions()
    data class OnPauseChange(val isPaused: Boolean): AnalisisScreenActions()
    data object OnClearData: AnalisisScreenActions()
}