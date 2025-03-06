package com.example.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.utils.StatusRobot
import com.example.hoverrobot.data.utils.ToolBox.toPercentLevel
import com.example.hoverrobot.databinding.AnalisisFragmentBinding
import com.example.hoverrobot.ui.analisisFragment.compose.LogScreen
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

    private var entryAnglePitch: ArrayList<Entry> = ArrayList()
    private var entryAngleRoll: ArrayList<Entry> = ArrayList()
    private var entryAngleYaw: ArrayList<Entry> = ArrayList()
    private var entrySpeedMotorL: ArrayList<Entry> = ArrayList()
    private var entrySpeedMotorR: ArrayList<Entry> = ArrayList()
    private var entryCurrentMotorL: ArrayList<Entry> = ArrayList()
    private var entryCurrentMotorR: ArrayList<Entry> = ArrayList()
    private var entrySetPointAngle: ArrayList<Entry> = ArrayList()
    private var entrySetPointPos: ArrayList<Entry> = ArrayList()
    private var entrySetPointYaw: ArrayList<Entry> = ArrayList()
    private var entrySetPointSpeed: ArrayList<Entry> = ArrayList()
    private var entryOutputYaw: ArrayList<Entry> = ArrayList()
    private var entryPosInMeters: ArrayList<Entry> = ArrayList()
    private var entryActualSpeed: ArrayList<Entry> = ArrayList()
    private var entryBatteryLevel: ArrayList<Entry> = ArrayList()

    private lateinit var lineDataAnglePitch: LineDataSet
    private lateinit var lineDataAngleRoll: LineDataSet
    private lateinit var lineDataAngleYaw: LineDataSet
    private lateinit var lineDataSpeedMotorL: LineDataSet
    private lateinit var lineDataSpeedMotorR: LineDataSet
    private lateinit var lineDataCurrentMotorL: LineDataSet
    private lateinit var lineDataCurrentMotorR: LineDataSet
    private lateinit var lineDataSetPointAngle: LineDataSet
    private lateinit var lineDataSetPointPos: LineDataSet
    private lateinit var lineDataSetPointYaw: LineDataSet
    private lateinit var lineDataSetPointSpeed: LineDataSet
    private lateinit var lineDataSetOutputYaw: LineDataSet
    private lateinit var lineDataSetPosInMeters: LineDataSet
    private lateinit var lineDataSetSpeed: LineDataSet
    private lateinit var lineDataBatteryLevel: LineDataSet

    private var actualLimitScale = 90F

    private var isAnalisisPaused = false

    private val statusCodeLogs = mutableStateOf<StatusRobot?>(null)

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

                LogScreen(newStatusRobot = statusCodeLogs)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGraph()
        setupObserver()
        setupListener()
    }

    private fun setupObserver() {

        analisisViewModel.newDataAnalisis.observe(viewLifecycleOwner) {
            it?.let {
                if (!isAnalisisPaused) {
                    newDynamicFrame(it)
                }

                statusCodeLogs.value = it.statusCode
            }
        }
    }

    private fun setupListener() {

        binding.switchAutoscale.setOnCheckedChangeListener { _, isChecked ->
//            setAutoScale(isChecked)
        }

        binding.rgDatasetSelect.setOnCheckedChangeListener { _, checkedId ->
            selectedDataset = when (checkedId) {
                R.id.rb_dataset_imu -> SelectedDataset.DATASET_IMU
                R.id.rb_dataset_power -> SelectedDataset.DATASET_POWER
                R.id.rb_dataset_pid_angle -> SelectedDataset.DATASET_PID_ANGLE
                R.id.rb_dataset_pid_pos -> SelectedDataset.DATASET_PID_POS
                R.id.rb_dataset_pid_yaw -> SelectedDataset.DATASET_PID_YAW
                R.id.rb_dataset_pid_speed -> SelectedDataset.DATASET_PID_SPEED
                R.id.rb_log_view -> null
                else -> SelectedDataset.DATASET_IMU
            }.also {
                binding.chart.isVisible = it != null
                binding.logsComposeView.isVisible = it == null
            }
        }

        binding.btnPlayPause.setOnClickListener {
            binding.btnPlayPause.text =
                getString(if (isAnalisisPaused) R.string.btn_pause_title else R.string.btn_play_title)
            isAnalisisPaused = !isAnalisisPaused
        }

        binding.btnClearData.setOnClickListener {
            entryAnglePitch.clear()
            entryAngleRoll.clear()
            entryAngleYaw.clear()
            entrySpeedMotorL.clear()
            entrySpeedMotorR.clear()
            entryCurrentMotorL.clear()
            entryCurrentMotorR.clear()
            entrySetPointAngle.clear()
            entrySetPointPos.clear()
            entrySetPointYaw.clear()
            entrySetPointSpeed.clear()
            entryOutputYaw.clear()
            entryPosInMeters.clear()
            entryActualSpeed.clear()
            entryBatteryLevel.clear()
        }
    }

    private fun initGraph() {
        with(binding.chart) {
            setOnChartValueSelectedListener(this@AnalisisFragment)
            setDrawGridBackground(false)
            description.isEnabled = false                             // Configura el eje
            setDrawBorders(true)

            axisLeft.isEnabled = true
            axisLeft.setDrawAxisLine(true)
            axisLeft.setDrawGridLines(true)

            axisRight.isEnabled = true
            axisRight.setDrawAxisLine(true)
            axisRight.setDrawGridLines(true)

            setAutoScale(binding.switchAutoscale.isChecked)

            xAxis.setDrawAxisLine(true)
            xAxis.setDrawGridLines(true)

            // enable touch gestures
            setTouchEnabled(true)

            // enable scaling and dragging
            isDragEnabled = true
            setScaleEnabled(true)

            // if disabled, scaling can be done on x- and y-axis separately
            setPinchZoom(false)

            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.form = Legend.LegendForm.CIRCLE
            legend.setDrawInside(true)
        }

        lineDataAnglePitch =
            createLineDataSet(entryAnglePitch, R.string.dataset_angle_pitch, R.color.red_80_percent)
        lineDataAngleRoll =
            createLineDataSet(entryAngleRoll, R.string.dataset_angle_roll, R.color.green_80_percent)
        lineDataAngleYaw =
            createLineDataSet(entryAngleYaw, R.string.dataset_angle_yaw, R.color.yellow_80_percent)
        lineDataSpeedMotorL =
            createLineDataSet(entrySpeedMotorL, R.string.dataset_speed_motor_l, R.color.blue_80_percent)
        lineDataSpeedMotorR =
            createLineDataSet(entrySpeedMotorR, R.string.dataset_speed_motor_r, R.color.red_80_percent)
        lineDataCurrentMotorL =
            createLineDataSet(entryCurrentMotorL, R.string.dataset_current_motor_l, R.color.status_orange)
        lineDataCurrentMotorR =
            createLineDataSet(entryCurrentMotorR, R.string.dataset_current_motor_r, R.color.status_green)
        lineDataSetPointAngle = createLineDataSet(
            entrySetPointAngle,
            R.string.dataset_set_point_angle,
            R.color.status_turquesa
        )
        lineDataSetPointPos = createLineDataSet(
            entrySetPointPos,
            R.string.dataset_set_point_pos,
            R.color.status_green
        )
        lineDataSetPointYaw = createLineDataSet(
            entrySetPointYaw,
            R.string.dataset_set_point_yaw,
            R.color.status_turquesa
        )
        lineDataSetPointSpeed = createLineDataSet(
            entrySetPointSpeed,
            R.string.dataset_set_point_speed,
            R.color.blue_80_percent
        )
        lineDataSetOutputYaw =
            createLineDataSet(entryOutputYaw, R.string.dataset_output_yaw, R.color.red_80_percent)
        lineDataSetPosInMeters = createLineDataSet(
            entryPosInMeters,
            R.string.dataset_position_meters,
            R.color.red_80_percent
        )
        lineDataSetSpeed =
            createLineDataSet(entryActualSpeed, R.string.dataset_speed, R.color.red_80_percent)
        lineDataBatteryLevel =
            createLineDataSet(entryBatteryLevel, R.string.dataset_battery_level, R.color.status_turquesa)

        initTimeStamp = System.currentTimeMillis()
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
    }

    private fun newDynamicFrame(newFrame: RobotDynamicData) {

        with(binding) {

            tvAnglePitch.text = getString(R.string.placeholder_pitch, newFrame.pitchAngle)
            tvAngleRoll.text = getString(R.string.placeholder_roll, newFrame.rollAngle)
            tvAngleYaw.text = getString(R.string.placeholder_yaw, newFrame.yawAngle)
            tvParamCenter.text = getString(R.string.placeholder_center, newFrame.centerAngle)
            tvPosition.text = getString(R.string.placeholder_position, newFrame.posInMeters)

            val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
            entryAnglePitch.add(Entry(actualTimeInSec, newFrame.pitchAngle))
            entryAngleRoll.add(Entry(actualTimeInSec, newFrame.rollAngle))
            entryAngleYaw.add(Entry(actualTimeInSec, newFrame.yawAngle))
            entrySpeedMotorL.add(Entry(actualTimeInSec, newFrame.speedL.toFloat() / 10))
            entrySpeedMotorR.add(Entry(actualTimeInSec, newFrame.speedR.toFloat() / 10))
            entryCurrentMotorL.add(Entry(actualTimeInSec, newFrame.currentL))
            entryCurrentMotorR.add(Entry(actualTimeInSec, newFrame.currentR))
            entrySetPointAngle.add(Entry(actualTimeInSec, newFrame.setPointAngle))
            entrySetPointPos.add(Entry(actualTimeInSec, newFrame.setPointPos))
            entrySetPointYaw.add(Entry(actualTimeInSec, newFrame.setPointYaw))
            entrySetPointSpeed.add(Entry(actualTimeInSec, newFrame.setPointSpeed))
            entryPosInMeters.add(Entry(actualTimeInSec, newFrame.posInMeters))
            entryOutputYaw.add(Entry(actualTimeInSec, newFrame.outputYawControl))
            entryActualSpeed.add(
                Entry(
                    actualTimeInSec,
                    newFrame.speedL.toFloat()
                )
            )           // TODO: tomar velocidad promedio
            entryBatteryLevel.add(Entry(actualTimeInSec, newFrame.batVoltage.toPercentLevel().toFloat()))

            lineDataAnglePitch.notifyDataSetChanged()
            lineDataAngleRoll.notifyDataSetChanged()
            lineDataAngleYaw.notifyDataSetChanged()
            lineDataSpeedMotorL.notifyDataSetChanged()
            lineDataSpeedMotorR.notifyDataSetChanged()
            lineDataCurrentMotorL.notifyDataSetChanged()
            lineDataCurrentMotorR.notifyDataSetChanged()
            lineDataSetPointAngle.notifyDataSetChanged()
            lineDataSetPointPos.notifyDataSetChanged()
            lineDataSetPointYaw.notifyDataSetChanged()
            lineDataSetPointSpeed.notifyDataSetChanged()
            lineDataSetOutputYaw.notifyDataSetChanged()
            lineDataSetPosInMeters.notifyDataSetChanged()
            lineDataSetSpeed.notifyDataSetChanged()
            lineDataBatteryLevel.notifyDataSetChanged()

            updateDataset(selectedDataset)
        }
    }

    private fun updateDataset(selectedDataset: SelectedDataset?) {
        selectedDataset?.let {
            binding.chart.data = when (selectedDataset) {
                SelectedDataset.DATASET_IMU -> {
                    setImuMode()
                    LineData(lineDataAnglePitch, lineDataAngleRoll, lineDataAngleYaw)
                }

                SelectedDataset.DATASET_POWER -> {
                    setMotorControlMode()
                    LineData(
                        lineDataSpeedMotorL,
                        lineDataSpeedMotorR,
                        lineDataCurrentMotorL,
                        lineDataCurrentMotorR,
                        lineDataBatteryLevel
                    )
                }

                SelectedDataset.DATASET_PID_ANGLE -> {
                    setPidAngleMode()
                    LineData(
                        lineDataSetPointAngle,
                        lineDataAnglePitch,
                        lineDataSpeedMotorL
                    )
                }

                SelectedDataset.DATASET_PID_POS -> {
                    setPidMode(10F)
                    LineData(lineDataSetPointPos, lineDataSetPosInMeters, lineDataSetPointAngle)
                }

                SelectedDataset.DATASET_PID_YAW -> {
                    setPidMode(190F)
                    LineData(lineDataSetPointYaw, lineDataSetOutputYaw, lineDataAngleYaw)
                }

                SelectedDataset.DATASET_PID_SPEED -> {
                    setPidMode(1050F)
                    LineData(lineDataSetPointSpeed, lineDataSetSpeed, lineDataSetPointAngle)
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
        setAutoScale(binding.switchAutoscale.isChecked)
        binding.chart.axisLeft.removeAllLimitLines()
    }

    private fun setImuMode() {

        actualLimitScale = 180F
        setAutoScale(binding.switchAutoscale.isChecked)

        binding.chart.axisLeft.removeAllLimitLines()
        val colorSafetyLimits =
            requireContext().getColor(androidx.appcompat.R.color.material_deep_teal_200)

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
        setAutoScale(binding.switchAutoscale.isChecked)

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
        setAutoScale(binding.switchAutoscale.isChecked)
        binding.chart.axisLeft.removeAllLimitLines()
    }
}

private const val FRAME_PERIOD = 0.05 // frecuencia de muestras en segundos

enum class SelectedDataset {
    DATASET_IMU,
    DATASET_POWER,
    DATASET_PID_ANGLE,
    DATASET_PID_POS,
    DATASET_PID_YAW,
    DATASET_PID_SPEED
}