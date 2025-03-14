package com.app.hoverrobot.ui.analisisFragment.compose

import com.app.hoverrobot.ui.analisisFragment.resources.SelectedDataset

sealed class AnalisisScreenActions {
    data class OnDatasetChange(val selectedDataset: SelectedDataset?): AnalisisScreenActions()
    data class OnPauseChange(val isPaused: Boolean): AnalisisScreenActions()
    data object OnClearData: AnalisisScreenActions()
}