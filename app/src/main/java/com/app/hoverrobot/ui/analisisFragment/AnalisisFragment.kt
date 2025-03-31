package com.app.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AnalisisFragment : Fragment() {

    private val analisisViewModel: AnalisisViewModel by viewModels(ownerProducer = { requireActivity() })

    private var selectedDataset: SelectedDataset? = SelectedDataset.DATASET_IMU

    private val entryMap: MutableMap<LineDataKeys, MutableList<Entry>> = mutableMapOf()
    private val lineDataMap: MutableMap<LineDataKeys, LineDataSet> = mutableMapOf()

    private val datasetKeys = LineDataKeys.entries.toList()

    private var actualLineData = mutableStateOf<LineData?>(null)
    private var chartLimitsConfig = mutableStateOf<ChartLimitsConfig>(ChartLimitsConfig(100F, null))

    private var isAnalisisPaused = false

    private val robotConfig: RobotLocalConfig?
        get() = analisisViewModel.newRobotConfig.value

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val lastDynamicData = analisisViewModel.newDataAnalisis
                .map { it.lastOrNull() } // Obtener el último elemento o null si está vacío
                .collectAsState(initial = null)

            AnalisisScreen(
                lastDynamicData = lastDynamicData,
                actualLineData = actualLineData,
                statusRobot = analisisViewModel.statusCode,
                chartLimitsConfig = chartLimitsConfig,
            ) { onAction ->
                when (onAction) {
                    is AnalisisScreenActions.OnDatasetChange -> {
                        selectedDataset = onAction.selectedDataset
                    }

                    is AnalisisScreenActions.OnPauseChange -> {
                        isAnalisisPaused = onAction.isPaused
                    }

                    is AnalisisScreenActions.OnClearData -> {
                        entryMap.values.forEach { it.clear() }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatasets()
        setupObserver()
    }

    private fun setupObserver() {

        lifecycleScope.launch {
            // Primero proceso todos los frames que corrieron sin el fragment abierto
            analisisViewModel.getStoredData().forEach { storedData ->
                newDynamicFrame(storedData)
            }

            // Una vez procesados los datos viejos, escucho los nuevos frames en tiempo real
            analisisViewModel.newDataAnalisis
                .onEach { newList ->
                    if (!isAnalisisPaused && newList.isNotEmpty()) {
                        newDynamicFrame(newList.last())
                    }
                }
                .launchIn(lifecycleScope) // Se ejecuta siempre en el MainThread y sigue el ciclo de vida
        }
    }

    private fun initDatasets() {
        datasetKeys.forEach { key ->
            val labelResId = datasetLabels[key] ?: R.string.dataset_default
            val colorResId = datasetColors[key] ?: R.color.black

            entryMap[key] = mutableListOf()  // Inicializamos la lista
            lineDataMap[key] = createLineDataSet(entryMap[key]!!, labelResId, colorResId)
        }
    }

    private fun createLineDataSet(
        entry: List<Entry>,
        labelResId: Int,
        colorResId: Int
    ): LineDataSet {
        return LineDataSet(entry, requireContext().getString(labelResId)).also {
            it.lineWidth = 2.5f
            it.circleRadius = 1f
            it.color = requireContext().getColor(colorResId)
            it.setDrawCircles(false)
            it.setDrawCircleHole(false)
            it.setDrawValues(false)
            it.setDrawHighlightIndicators(false)
            it.isHighlightEnabled = false
        }
    }

    private fun newDynamicFrame(newFrame: FrameRobotDynamicData) {
        lifecycleScope.launch(Dispatchers.Main) {
            entryMap.updateWithFrame(newFrame)
            lineDataMap.values.forEach { it.notifyDataSetChanged() }
            updateDataset(selectedDataset)
        }
    }

    private fun updateDataset(selectedDataset: SelectedDataset?) {
        selectedDataset?.let {
            actualLineData.value = when (selectedDataset) {
                SelectedDataset.DATASET_IMU -> {
                    setImuMode()
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ANGLE_PITCH],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ANGLE_ROLL],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ANGLE_YAW]
                    )
                }

                SelectedDataset.DATASET_POWER -> {
                    setMotorControlMode()
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SPEED_L],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SPEED_R],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_CURRENT_L],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_CURRENT_R],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_BATTERY_LVL]
                    )
                }

                SelectedDataset.DATASET_PID_ANGLE -> {
                    setPidAngleMode()
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ANGLE_PITCH],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SPEED_L]
                    )
                }

                SelectedDataset.DATASET_PID_POS -> {
                    setPidMode(10F)
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_POS],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_POS_IN_MTS],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE]
                    )
                }

                SelectedDataset.DATASET_PID_YAW -> {
                    setPidMode(190F)
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_YAW],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_OUTPUT_YAW],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ANGLE_YAW]
                    )
                }

                SelectedDataset.DATASET_PID_SPEED -> {
                    setPidMode(1050F)
                    LineData(
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_SPEED],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_ACTUAL_SPEED],
                        lineDataMap[LineDataKeys.LINEDATA_KEY_SETPOINT_ANGLE]
                    )
                }
            }
        }
    }

    private fun setMotorControlMode() {
        chartLimitsConfig.value = ChartLimitsConfig(1000F, null)
    }

    private fun setImuMode() {
        val colorSafetyLimits = requireContext().getColor(R.color.status_turquesa)

        robotConfig?.let { robotConfig ->

            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "Centro de gravedad")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)

            val upperLimitLine =
                LimitLine(
                    robotConfig.centerAngle + robotConfig.safetyLimits,
                    "Limite seguridad superior"
                )
            upperLimitLine.lineWidth = 2f
            upperLimitLine.enableDashedLine(20f, 10f, 10f)
            upperLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            upperLimitLine.textSize = 10f
            upperLimitLine.lineColor = colorSafetyLimits

            val lowerLimitLine = LimitLine(
                robotConfig.centerAngle - robotConfig.safetyLimits,
                "Limite seguridad inferior"
            )
            lowerLimitLine.lineWidth = 2f
            lowerLimitLine.enableDashedLine(20f, 10f, 10f)
            lowerLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            lowerLimitLine.textSize = 10f
            lowerLimitLine.lineColor = colorSafetyLimits

            chartLimitsConfig.value = ChartLimitsConfig(
                180F,
                listOf(centerAngleLimitLine, upperLimitLine, lowerLimitLine)
            )
        }
    }

    private fun setPidAngleMode() {
        robotConfig?.let { robotConfig ->
            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "center Angle")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)

            chartLimitsConfig.value = ChartLimitsConfig(15F, null)
        }
    }

    private fun setPidMode(limit: Float) {
        chartLimitsConfig.value = ChartLimitsConfig(limit, null)
    }
}

