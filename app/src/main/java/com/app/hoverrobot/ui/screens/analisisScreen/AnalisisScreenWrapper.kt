package com.app.hoverrobot.ui.screens.analisisScreen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.screens.analisisScreen.resources.EntriesMaps.datasetColors
import com.app.hoverrobot.ui.screens.analisisScreen.resources.EntriesMaps.datasetLabels

@Composable
fun AnalisisScreenWrapper(
    analisisViewModel: AnalisisViewModel,
    onActionAnalisisScreen: (AnalisisScreenActions) -> Unit
) {
    val activity = LocalContext.current as AppCompatActivity

    LaunchedEffect(Unit) {
        if (!analisisViewModel.isGraphInitialize) {
            val context = activity.applicationContext
            analisisViewModel.datasetKeys.forEach { key ->
                val labelResId = datasetLabels[key] ?: R.string.dataset_default
                val colorResId = datasetColors[key] ?: R.color.black

                analisisViewModel.lineDataMap[key] = analisisViewModel.createLineDataSet(
                    analisisViewModel.entryMap[key]!!,
                    context.getString(labelResId),
                    context.getColor(colorResId)
                )
            }

            analisisViewModel.initGraph(
                context.getColor(R.color.status_turquesa),
                context.getColor(R.color.status_blue)
            )
        }
    }

    AnalisisScreen(
        lastDynamicData = analisisViewModel.newDataAnalisis.collectAsState(),
        actualLineData = analisisViewModel.actualLineData,
        historicStatusRobot = analisisViewModel.historicStatusCode,
        chartLimitsConfig = analisisViewModel.chartLimitsConfig,
        onActionAnalisisScreen = onActionAnalisisScreen
    )
}
