package com.app.hoverrobot.ui.screens.analisisScreen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.hoverrobot.data.models.ChartLimitsConfig
import com.app.hoverrobot.data.models.comms.FrameRobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.repositories.CommsRepository
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.ui.screens.analisisScreen.resources.EntriesMaps.updateWithFrame
import com.app.hoverrobot.ui.screens.analisisScreen.resources.LineDataKeys
import com.app.hoverrobot.ui.screens.analisisScreen.resources.SelectedDataset
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val commsRepository: CommsRepository
): ViewModel() {

    private val _newDataAnalisis = MutableStateFlow<FrameRobotDynamicData?>(null)
    val newDataAnalisis : StateFlow<FrameRobotDynamicData?> get() = _newDataAnalisis

    private var _newRobotConfig : MutableLiveData<RobotLocalConfig> = MutableLiveData()
    val newRobotConfig : LiveData<RobotLocalConfig> get() = _newRobotConfig

    private var _statusCode = mutableStateOf<StatusRobot?>(null)
    val statusCode : State<StatusRobot?> get() = _statusCode

    private val initTimeStamp: Long = System.currentTimeMillis()

    private var selectedDataset: SelectedDataset? = SelectedDataset.DATASET_IMU

    val entryMap: MutableMap<LineDataKeys, MutableList<Entry>> = LineDataKeys.entries
        .associateWith { mutableListOf<Entry>() }
        .toMutableMap()

    val lineDataMap: MutableMap<LineDataKeys, LineDataSet> = mutableMapOf()
    val datasetKeys = LineDataKeys.entries.toList()
    var actualLineData = mutableStateOf<LineData?>(null)
    var chartLimitsConfig = mutableStateOf<ChartLimitsConfig>(ChartLimitsConfig(100F, null))
    private var isAnalisisPaused = false

    var isGraphInitialize = false
        internal set


    var colorSafetyLimits: Int = 0 // TODO: eliminar esto
        internal set

    var colorCenterAngle: Int = 0 // TODO: eliminar esto
        internal set

    init {
        ioScope.launch {
            commsRepository.dynamicDataRobotFlow.collect { newData ->
                val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
                _newDataAnalisis.value = FrameRobotDynamicData(newData,actualTimeInSec)
                _statusCode.value = newData.statusCode

                if (isGraphInitialize && _newDataAnalisis.value != null) newDynamicFrame(_newDataAnalisis.value!!)
            }
        }

        ioScope.launch {
            commsRepository.robotLocalConfigFlow.collect {
                it?.let {
                    _newRobotConfig.postValue(it)
                }
            }
        }
    }

    fun changeSelectedDataset(datasetSelected: SelectedDataset?) {
        selectedDataset = datasetSelected
    }

    fun clearChart() {
        entryMap.values.forEach { it.clear() }
    }

    fun setPaused(isPaused: Boolean) {
        isAnalisisPaused = isPaused
    }

    fun initGraph(colorSafetyLimit: Int, colorCenterAngle: Int,) {
        this.colorSafetyLimits = colorSafetyLimit
        this.colorCenterAngle = colorCenterAngle
        isGraphInitialize = true
    }

    fun createLineDataSet(
        entry: List<Entry>,
        label: String,
        color: Int
    ): LineDataSet {
        return LineDataSet(entry, label).also {
            it.lineWidth = 2.5f
            it.circleRadius = 1f
            it.color = color
            it.setDrawCircles(false)
            it.setDrawCircleHole(false)
            it.setDrawValues(false)
            it.setDrawHighlightIndicators(false)
            it.isHighlightEnabled = false
        }
    }

    private fun newDynamicFrame(newFrame: FrameRobotDynamicData) {
        if (isGraphInitialize) {
            entryMap.updateWithFrame(newFrame)
            lineDataMap.values.forEach { it.notifyDataSetChanged() }
            if (!isAnalisisPaused) updateDataset(selectedDataset)
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

        newRobotConfig.value?.let { robotConfig ->

            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "Centro de gravedad")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = colorCenterAngle

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
        newRobotConfig.value?.let { robotConfig ->
            val centerAngleLimitLine = LimitLine(robotConfig.centerAngle, "center Angle")
            centerAngleLimitLine.lineWidth = 2f
            centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
            centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            centerAngleLimitLine.textSize = 10f
            centerAngleLimitLine.lineColor = colorCenterAngle

            chartLimitsConfig.value = ChartLimitsConfig(15F, null)
        }
    }

    private fun setPidMode(limit: Float) {
        chartLimitsConfig.value = ChartLimitsConfig(limit, null)
    }
}