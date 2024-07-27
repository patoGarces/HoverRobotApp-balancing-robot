package com.example.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.RobotDynamicData
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.databinding.AnalisisFragmentBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class AnalisisFragment : Fragment(), OnChartValueSelectedListener {

    private val analisisViewModel: AnalisisViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding: AnalisisFragmentBinding
    private val binding get() = _binding

    private var initTimeStamp: Long = 0
    private var frameSize = (15.00 / FRAME_PERIOD).toInt()
    private var viewDataset: DatasetView = DatasetView.DATASET_IMU

    private var dataSet: LineData? = null

    private var entryAnglePitch: ArrayList<Entry> = ArrayList()
    private var entryAngleRoll: ArrayList<Entry> = ArrayList()
    private var entryAngleYaw: ArrayList<Entry> = ArrayList()
    private var entryMotorL: ArrayList<Entry> = ArrayList()
    private var entryMotorR: ArrayList<Entry> = ArrayList()
    private var entrySetPoint: ArrayList<Entry> = ArrayList()

    private var actualLimitScale = 90F

    private var isAnalisisPaused = false

    private val robotConfig: RobotLocalConfig?
        get() = analisisViewModel.newRobotConfig.value

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AnalisisFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
            }
        }
    }

    private fun setupListener() {

        binding.switchAutoscale.setOnCheckedChangeListener { _, isChecked ->
            setAutoScale(isChecked)
        }

        binding.rgSizeSamples.setOnCheckedChangeListener { _, checkedId ->
            frameSize = when (checkedId) {
                R.id.rb_samples_5s -> (5.00 / FRAME_PERIOD).toInt()
                R.id.rb_samples_15s -> (15.00 / FRAME_PERIOD).toInt()
                R.id.rb_samples_30s -> (30.00 / FRAME_PERIOD).toInt()
                else -> (15.00 / FRAME_PERIOD).toInt()
            }
        }

        binding.rgDatasetSelect.setOnCheckedChangeListener { _, checkedId ->
            viewDataset = when (checkedId) {
                R.id.rb_dataset_imu -> DatasetView.DATASET_IMU
                R.id.rb_dataset_motor -> DatasetView.DATASET_MOTOR
                R.id.rb_dataset_pid -> DatasetView.DATASET_PID
                else -> DatasetView.DATASET_IMU
            }
        }

        binding.btnPlayPause.setOnClickListener {

            binding.btnPlayPause.text = getString(if(isAnalisisPaused) R.string.btn_pause_title else R.string.btn_play_title)
            isAnalisisPaused = !isAnalisisPaused
        }

        binding.btnClearData.setOnClickListener {
            entryAnglePitch.clear()
            entryAngleRoll.clear()
            entryAngleYaw.clear()
            entryMotorL.clear()
            entryMotorR.clear()
            entrySetPoint.clear()
        }

        binding.btnGenerateDataset.setOnClickListener {
            for (i in 0..100) {

                val randomData = RobotDynamicData(
                    batVoltage = (Math.random() * 100).toFloat(),
                    tempImu = (Math.random() * 100).toFloat(),
                    speedR = Math.random().toInt(),
                    speedL = Math.random().toInt(),
                    pitchAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    rollAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    yawAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    setPoint = ((Math.random() - 0.5) * 180).toFloat(),
                    centerAngle = ((Math.random() - 0.5) * 180).toFloat(),
                    statusCode = 0,
                )
                newDynamicFrame(randomData)
            }
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
        initTimeStamp = System.currentTimeMillis()
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
            with(binding) {
                tvAnglePitch.text = getString(R.string.placeholder_pitch, newFrame.pitchAngle)
                tvAngleRoll.text = getString(R.string.placeholder_roll, newFrame.rollAngle)
                tvAngleYaw.text = getString(R.string.placeholder_yaw, newFrame.yawAngle)
                tvParamCenter.text = getString(R.string.placeholder_center, newFrame.centerAngle)
            }

            val lineDataAnglePitch =
                LineDataSet(entryAnglePitch.takeLast(frameSize), "Pitch Angle").also {
                    it.lineWidth = 2.5f
                    it.circleRadius = 1f
                    it.color = requireContext().getColor(R.color.red_80_percent)
                    it.setCircleColor(it.color)
                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label

            val lineDataAngleRoll =
                LineDataSet(entryAngleRoll.takeLast(frameSize), "Roll Angle").also {
                    it.lineWidth = 2.5f
                    it.circleRadius = 1f
                    it.color = requireContext().getColor(R.color.green_80_percent)
                    it.setCircleColor(it.color)
                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label

            val lineDataAngleYaw =
                LineDataSet(entryAngleYaw.takeLast(frameSize), "Yaw Angle").also {
                    it.lineWidth = 2.5f
                    it.circleRadius = 1f
                    it.color = requireContext().getColor(R.color.yellow_80_percent)
                    it.setCircleColor(it.color)
                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label

            val lineDataMotorL = LineDataSet(entryMotorL.takeLast(frameSize), "Motor L x100").also {
                it.lineWidth = 2.5f
                it.circleRadius = 1f
                it.color = requireContext().getColor(R.color.blue_80_percent)
                it.setCircleColor(it.color)
            }

            val lineDataMotorR = LineDataSet(entryMotorR.takeLast(frameSize), "Motor R x100").also {
                it.lineWidth = 2.5f
                it.circleRadius = 1f
                it.color = requireContext().getColor(R.color.red_80_percent)
                it.setCircleColor(it.color)
            }

            val lineDataSetPoint = LineDataSet(entrySetPoint.takeLast(frameSize), "setPoint").also {
                it.lineWidth = 2.5f
                it.circleRadius = 1f
                it.color = requireContext().getColor(R.color.status_turquesa)
                it.setCircleColor(it.color)
            }
            val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
            entryAnglePitch.add(Entry(actualTimeInSec, newFrame.pitchAngle))
            entryAngleRoll.add(Entry(actualTimeInSec, newFrame.rollAngle))
            entryAngleYaw.add(Entry(actualTimeInSec, newFrame.yawAngle))
            entryMotorL.add(Entry(actualTimeInSec, newFrame.speedL.toFloat() / 100))
            entryMotorR.add(Entry(actualTimeInSec, newFrame.speedR.toFloat() / 100))
            entrySetPoint.add(Entry(actualTimeInSec, newFrame.setPoint))

            if (entrySetPoint.size >= frameSize) {
                entryAnglePitch.removeFirst()
                entryAngleRoll.removeFirst()
                entryAngleYaw.removeFirst()
                entryMotorL.removeFirst()
                entryMotorR.removeFirst()
                entrySetPoint.removeFirst()
            }

            robotConfig?.let {
                tvParamKp.text = getString(R.string.placeholder_kp, it.kp.toString())
                tvParamKi.text = getString(R.string.placeholder_ki, it.ki.toString())
                tvParamKd.text = getString(R.string.placeholder_kd, it.kd.toString())
            }

            dataSet = when (viewDataset) {
                DatasetView.DATASET_IMU -> {
                    setImuMode(newFrame)
                    val arrayImuDataset = ArrayList<ILineDataSet>()
                    arrayImuDataset.clear()
                    arrayImuDataset.add(lineDataAnglePitch)
                    arrayImuDataset.add(lineDataAngleRoll)
                    arrayImuDataset.add(lineDataAngleYaw)
                    LineData(arrayImuDataset)
                }

                DatasetView.DATASET_MOTOR -> {
                    setMotorControlMode()
                    val arrayMotorDataset = ArrayList<ILineDataSet>()
                    arrayMotorDataset.clear()
                    arrayMotorDataset.add(lineDataMotorL)
                    arrayMotorDataset.add(lineDataMotorR)
                    LineData(arrayMotorDataset)
                }

                DatasetView.DATASET_PID -> {
                    setPidMode(newFrame)
                    val arrayPidDataset = ArrayList<ILineDataSet>()
                    arrayPidDataset.clear()
                    arrayPidDataset.add(lineDataSetPoint)
                    arrayPidDataset.add(lineDataAnglePitch)
                    arrayPidDataset.add(lineDataAngleRoll)
                    arrayPidDataset.add(lineDataMotorL)
                    LineData(arrayPidDataset)
                }
            }

            chart.data = dataSet
            chart.invalidate()
        }
    }

//    private fun newAngle(newFrame: MainBoardRobotStatus) {
//
//        with(binding) {
//            tvParamKp.text = getString(R.string.placeholder_kp, newFrame.pid.kp.toString())
//            tvParamKi.text = getString(R.string.placeholder_ki, newFrame.pid.ki.toString())
//            tvParamKd.text = getString(R.string.placeholder_kd, newFrame.pid.kd.toString())
//            tvParamCenter.text = getString(R.string.placeholder_center, newFrame.pid.centerAngle.toString())
//
//            val lineDataAnglePitch =
//                LineDataSet(entryAnglePitch.takeLast(frameSize), "Pitch Angle").also {
//                    it.lineWidth = 2.5f
//                    it.circleRadius = 1f
//                    it.color = requireContext().getColor(R.color.red_80_percent)
//                    it.setCircleColor(it.color)
//                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label
//
//            val lineDataAngleRoll =
//                LineDataSet(entryAngleRoll.takeLast(frameSize), "Roll Angle").also {
//                    it.lineWidth = 2.5f
//                    it.circleRadius = 1f
//                    it.color = requireContext().getColor(R.color.green_80_percent)
//                    it.setCircleColor(it.color)
//                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label
//
//            val lineDataAngleYaw =
//                LineDataSet(entryAngleYaw.takeLast(frameSize), "Yaw Angle").also {
//                    it.lineWidth = 2.5f
//                    it.circleRadius = 1f
//                    it.color = requireContext().getColor(R.color.yellow_80_percent)
//                    it.setCircleColor(it.color)
//                }                                                                                           // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label
//
//            val lineDataMotorL = LineDataSet(entryMotorL.takeLast(frameSize), "Motor L x100").also {
//                it.lineWidth = 2.5f
//                it.circleRadius = 1f
//                it.color = requireContext().getColor(R.color.blue_80_percent)
//                it.setCircleColor(it.color)
//            }
//
//            val lineDataMotorR = LineDataSet(entryMotorR.takeLast(frameSize), "Motor R x100").also {
//                it.lineWidth = 2.5f
//                it.circleRadius = 1f
//                it.color = requireContext().getColor(R.color.red_80_percent)
//                it.setCircleColor(it.color)
//            }
//
//            val lineDataSetPoint = LineDataSet(entrySetPoint.takeLast(frameSize), "setPoint").also {
//                it.lineWidth = 2.5f
//                it.circleRadius = 1f
//                it.color = requireContext().getColor(R.color.status_turquesa)
//                it.setCircleColor(it.color)
//            }
//            val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat()) / 1000
//            entryAnglePitch.add(Entry(actualTimeInSec, newFrame.pitchAngle))
//            entryAngleRoll.add(Entry(actualTimeInSec, newFrame.rollAngle))
//            entryAngleYaw.add(Entry(actualTimeInSec, newFrame.yawAngle))
//            entryMotorL.add(Entry(actualTimeInSec, newFrame.speedL.toFloat() / 100))
//            entryMotorR.add(Entry(actualTimeInSec, newFrame.speedR.toFloat() / 100))
//            entrySetPoint.add(Entry(actualTimeInSec, newFrame.setPoint))
//
//            if (entrySetPoint.size >= frameSize) {
//                entryAnglePitch.removeFirst()
//                entryAngleRoll.removeFirst()
//                entryAngleYaw.removeFirst()
//                entryMotorL.removeFirst()
//                entryMotorR.removeFirst()
//                entrySetPoint.removeFirst()
//            }
//
//            dataSet = when (viewDataset) {
//                DatasetView.DATASET_IMU -> {
//                    setImuMode(newFrame)
//                    val arrayImuDataset = ArrayList<ILineDataSet>()
//                    arrayImuDataset.clear()
//                    arrayImuDataset.add(lineDataAnglePitch)
//                    arrayImuDataset.add(lineDataAngleRoll)
//                    arrayImuDataset.add(lineDataAngleYaw)
//                    LineData(arrayImuDataset)
//                }
//
//                DatasetView.DATASET_MOTOR -> {
//                    setMotorControlMode()
//                    val arrayMotorDataset = ArrayList<ILineDataSet>()
//                    arrayMotorDataset.clear()
//                    arrayMotorDataset.add(lineDataMotorL)
//                    arrayMotorDataset.add(lineDataMotorR)
//                    LineData(arrayMotorDataset)
//                }
//
//                DatasetView.DATASET_PID -> {
//                    setPidMode(newFrame)
//                    val arrayPidDataset = ArrayList<ILineDataSet>()
//                    arrayPidDataset.clear()
//                    arrayPidDataset.add(lineDataSetPoint)
//                    arrayPidDataset.add(lineDataAnglePitch)
//                    arrayPidDataset.add(lineDataMotorL)
//                    LineData(arrayPidDataset)
//                }
//            }
//
//            chart.data = dataSet
//            chart.invalidate()
//        }
//    }


    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(
            "VAL SELECTED",
            "Value: " + e?.y + ", xIndex: " + e?.x + ", DataSet index: " + h?.dataSetIndex
        )
    }

    override fun onNothingSelected() {

    }

    private fun setMotorControlMode() {
        actualLimitScale = 12F
        setAutoScale(binding.switchAutoscale.isChecked)
        binding.chart.axisLeft.removeAllLimitLines()
    }

    private fun setImuMode(actualLineLimits: RobotDynamicData) {

        actualLimitScale = 100F
        setAutoScale(binding.switchAutoscale.isChecked)

        binding.chart.axisLeft.removeAllLimitLines()
        val colorSafetyLimits =
            requireContext().getColor(androidx.appcompat.R.color.material_deep_teal_200)

        val centerAngleLimitLine = LimitLine(actualLineLimits.centerAngle, "Centro de gravedad")
        centerAngleLimitLine.lineWidth = 2f
        centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
        centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        centerAngleLimitLine.textSize = 10f
        centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)
        binding.chart.axisLeft.addLimitLine(centerAngleLimitLine)

        robotConfig?.let { robotConfig ->
            val upperLimitLine =
                LimitLine(
                    actualLineLimits.centerAngle + robotConfig.safetyLimits,
                    "Limite seguridad superior"
                )
            upperLimitLine.lineWidth = 2f
            upperLimitLine.enableDashedLine(20f, 10f, 10f)
            upperLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
            upperLimitLine.textSize = 10f
            upperLimitLine.lineColor = colorSafetyLimits
            binding.chart.axisLeft.addLimitLine(upperLimitLine)

            val lowerLimitLine = LimitLine(
                actualLineLimits.centerAngle - robotConfig.safetyLimits,
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

    private fun setPidMode(newFrame: RobotDynamicData) {
        actualLimitScale = 15F
        setAutoScale(binding.switchAutoscale.isChecked)

        binding.chart.axisLeft.removeAllLimitLines()

        val centerAngleLimitLine = LimitLine(newFrame.centerAngle, "Centro de gravedad")
        centerAngleLimitLine.lineWidth = 2f
        centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
        centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        centerAngleLimitLine.textSize = 10f
        centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)
        binding.chart.axisLeft.addLimitLine(centerAngleLimitLine)
    }
}

private const val FRAME_PERIOD = 0.05 // frecuencia de muestras en segundos

enum class DatasetView {
    DATASET_IMU,
    DATASET_MOTOR,
    DATASET_PID
}