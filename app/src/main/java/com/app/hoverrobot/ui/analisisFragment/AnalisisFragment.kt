package com.app.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.ChartLimitsConfig
import com.app.hoverrobot.data.models.comms.FrameRobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.ui.analisisFragment.compose.AnalisisScreen
import com.app.hoverrobot.ui.analisisFragment.compose.AnalisisScreenActions
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.datasetColors
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.datasetLabels
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.updateWithFrame
import com.app.hoverrobot.ui.analisisFragment.resources.LineDataKeys
import com.app.hoverrobot.ui.analisisFragment.resources.SelectedDataset
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalisisFragment : Fragment() {

    private val analisisViewModel: AnalisisViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            AnalisisScreen(
                lastDynamicData = analisisViewModel.newDataAnalisis.collectAsState(),
                actualLineData = analisisViewModel.actualLineData,
                statusRobot = analisisViewModel.statusCode,
                chartLimitsConfig = analisisViewModel.chartLimitsConfig,
            ) { onAction ->
                when (onAction) {
                    is AnalisisScreenActions.OnDatasetChange -> {
                        analisisViewModel.changeSelectedDataset(onAction.selectedDataset)
                    }

                    is AnalisisScreenActions.OnPauseChange -> {
                        analisisViewModel.setPaused(onAction.isPaused)
                    }

                    is AnalisisScreenActions.OnClearData -> {
                        analisisViewModel.clearChart()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!analisisViewModel.isGraphInitialize) initDatasets()
    }

    // TODO: ta horrible, hacer refactor
    private fun initDatasets() {
        analisisViewModel.datasetKeys.forEach { key ->
            val labelResId = datasetLabels[key] ?: R.string.dataset_default
            val colorResId = datasetColors[key] ?: R.color.black

            analisisViewModel.lineDataMap[key] = analisisViewModel.createLineDataSet(
                analisisViewModel.entryMap[key]!!,
                getString(labelResId),
                requireContext().getColor(colorResId)
            )
        }

        analisisViewModel.initGraph(
            requireContext().getColor(R.color.status_turquesa),
            requireContext().getColor(R.color.status_blue)

        )
    }
}