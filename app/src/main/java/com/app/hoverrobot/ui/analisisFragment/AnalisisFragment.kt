package com.app.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.databinding.AnalisisFragmentBinding
import com.app.hoverrobot.ui.analisisFragment.compose.LogScreen
import com.app.hoverrobot.ui.analisisFragment.compose.SettingsMenuActions
import com.app.hoverrobot.ui.analisisFragment.compose.SettingsMenuScreen
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.datasetColors
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.datasetLabels
import com.app.hoverrobot.ui.analisisFragment.resources.EntriesMaps.updateWithFrame
import com.app.hoverrobot.ui.analisisFragment.resources.LineDataKeys
import com.app.hoverrobot.ui.analisisFragment.resources.SelectedDataset
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class AnalisisFragment : Fragment(), OnChartValueSelectedListener {

    private val analisisViewModel: AnalisisViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding: AnalisisFragmentBinding
    private val binding get() = _binding

    private var initTimeStamp: Long = 0
    private var selectedDataset: SelectedDataset? = SelectedDataset.DATASET_IMU

    private val entryMap: MutableMap<LineDataKeys, MutableList<Entry>> = mutableMapOf()
    private val lineDataMap: MutableMap<LineDataKeys, LineDataSet> = mutableMapOf()

    private val datasetKeys = LineDataKeys.entries.toList()

    private var actualLimitScale = 90F

    private var isAnalisisPaused = false
    private var isAutoScaleEnabled = false

    private val robotConfig: RobotLocalConfig?
        get() = analisisViewModel.newRobotConfig.value

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AnalisisFragmentBinding.inflate(inflater, container, false)

        binding.logsComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                LogScreen(newStatusRobot = analisisViewModel.statusCode)
            }
        }

        binding.settingsChart.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SettingsMenuScreen { onAction ->
                    when (onAction) {
                        is SettingsMenuActions.OnAutoScaleChange -> {
                            setAutoScale(onAction.isEnable)
                        }

                        is SettingsMenuActions.OnDatasetChange -> {
                            selectedDataset = onAction.selectedDataset

                            binding.chart.isVisible = selectedDataset != null
                            binding.logsComposeView.isVisible = selectedDataset == null
                        }

                        is SettingsMenuActions.OnPauseChange -> {
                            isAnalisisPaused = onAction.isPaused
                        }

                        is SettingsMenuActions.OnClearData -> {
                            entryMap.values.forEach { it.clear() }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        initDatasets()
        setupObserver()
    }

    private fun setupObserver() {

        analisisViewModel.newDataAnalisis.observe(viewLifecycleOwner) {
            it?.let {
                if (!isAnalisisPaused) {
                    newDynamicFrame(it)
                }
            }
        }
    }

    private fun setupChart() {
        with(binding.chart) {
            setDrawGridBackground(false)
            description.isEnabled = false
            setDrawBorders(true)
            configureAxes()
            enableTouchAndZoom()
            configureLegend()
        }
        initTimeStamp = System.currentTimeMillis()
    }

    private fun configureAxes() {
        with(binding.chart) {
            axisLeft.isEnabled = true
            axisLeft.setDrawAxisLine(true)
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = true
            axisRight.setDrawAxisLine(true)
            axisRight.setDrawGridLines(true)
            xAxis.setDrawAxisLine(true)
            xAxis.setDrawGridLines(true)
        }
    }

    private fun enableTouchAndZoom() {
        with(binding.chart) {
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
        }
    }

    private fun configureLegend() {
        with(binding.chart.legend) {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            form = Legend.LegendForm.CIRCLE
            setDrawInside(true)
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
            it.setCircleColor(it.color)
        }
    }

    private fun setAutoScale(autoScale: Boolean) {
        with(binding) {
            if (!autoScale) {
                chart.axisLeft.axisMinimum = -actualLimitScale
                chart.axisLeft.axisMaximum = actualLimitScale
                chart.axisRight.axisMinimum = -actualLimitScale
                chart.axisRight.axisMaximum = actualLimitScale
            } else {
                chart.axisLeft.resetAxisMaximum()
                chart.axisLeft.resetAxisMinimum()
                chart.axisRight.resetAxisMaximum()
                chart.axisRight.resetAxisMinimum()
            }
        }
        isAutoScaleEnabled = autoScale
    }

    private fun newDynamicFrame(newFrame: RobotDynamicData) {
        with(binding) {
            tvAnglePitch.text = getString(R.string.placeholder_pitch, newFrame.pitchAngle)
            tvAngleRoll.text = getString(R.string.placeholder_roll, newFrame.rollAngle)
            tvAngleYaw.text = getString(R.string.placeholder_yaw, newFrame.yawAngle)
            tvParamCenter.text = getString(R.string.placeholder_center, newFrame.centerAngle)
            tvPosition.text = getString(R.string.placeholder_position, newFrame.posInMeters)

            val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
            entryMap.updateWithFrame(actualTimeInSec, newFrame)

            lineDataMap.values.forEach { it.notifyDataSetChanged() }
            updateDataset(selectedDataset)
        }
    }

    private fun updateDataset(selectedDataset: SelectedDataset?) {
        selectedDataset?.let {
            binding.chart.data = when (selectedDataset) {
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
        binding.chart.notifyDataSetChanged()
        binding.chart.invalidate()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(
            "VAL SELECTED",
            "Value: " + e?.y + ", xIndex: " + e?.x + ", DataSet index: " + h?.dataSetIndex
        )
    }

    override fun onNothingSelected() {}

    private fun setMotorControlMode() {
        actualLimitScale = 1000F
        setAutoScale(isAutoScaleEnabled)
        binding.chart.axisLeft.removeAllLimitLines()
    }

    private fun setImuMode() {

        actualLimitScale = 180F
        setAutoScale(isAutoScaleEnabled)
        binding.chart.axisLeft.removeAllLimitLines()
        val colorSafetyLimits = requireContext().getColor(R.color.status_turquesa)

        robotConfig?.let { robotConfig ->

            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "Centro de gravedad")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)
            binding.chart.axisLeft.addLimitLine(centerAngleLimitLine)

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
            binding.chart.axisLeft.addLimitLine(upperLimitLine)

            val lowerLimitLine = LimitLine(
                robotConfig.centerAngle - robotConfig.safetyLimits,
                "Limite seguridad inferior"
            )
            lowerLimitLine.lineWidth = 2f
            lowerLimitLine.enableDashedLine(20f, 10f, 10f)
            lowerLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            lowerLimitLine.textSize = 10f
            lowerLimitLine.lineColor = colorSafetyLimits
            binding.chart.axisLeft.addLimitLine(lowerLimitLine)
        }
    }

    private fun setPidAngleMode() {
        actualLimitScale = 15F
        setAutoScale(isAutoScaleEnabled)
        binding.chart.axisLeft.removeAllLimitLines()

        robotConfig?.let { robotConfig ->
            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "center Angle")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)
            binding.chart.axisLeft.addLimitLine(centerAngleLimitLine)
        }
    }

    private fun setPidMode(limit: Float) {
        actualLimitScale = limit
        setAutoScale(isAutoScaleEnabled)
        binding.chart.axisLeft.removeAllLimitLines()
    }
}

