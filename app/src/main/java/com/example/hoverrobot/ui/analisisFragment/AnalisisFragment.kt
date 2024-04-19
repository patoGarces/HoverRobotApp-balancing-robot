package com.example.hoverrobot.ui.analisisFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.Models.comms.MainBoardResponse
import com.example.hoverrobot.R
import com.example.hoverrobot.databinding.AnalisisFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class AnalisisFragment : Fragment(), OnChartValueSelectedListener {//},OnChartGestureListener{

    private val analisisViewModel : AnalisisViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding : AnalisisFragmentBinding
    private val binding get() = _binding

    private var initTimeStamp : Long = 0
    private var frameSize = 10
    private var viewDataset : DatasetView = DatasetView.DATASET_IMU

    private lateinit var chart : LineChart
    private var dataSet : LineData? = null

    private var arrayImuDataset = ArrayList<ILineDataSet>()
    private var entryAnglePitch : ArrayList<Entry>? = null
    private var lineDataAnglePitch : LineDataSet? = null
    private var entryAngleRoll : ArrayList<Entry>? = null
    private var lineDataAngleRoll : LineDataSet? = null
    private var entryAngleYaw : ArrayList<Entry>? = null
    private var lineDataAngleYaw : LineDataSet? = null

    private var arrayMotorDataset = ArrayList<ILineDataSet>()

    private var entryMotorL : ArrayList<Entry>? = null
    private var lineDataMotorL : LineDataSet? = null
    private var entryMotorR : ArrayList<Entry>? = null
    private var lineDataMotorR : LineDataSet? = null

    private var arrayPidDataset = ArrayList<ILineDataSet>()

    private var actualLimitScale = 90F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AnalisisFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initGraph()
        setupObserver()
        setupListener()
    }

    private fun setupObserver(){

        analisisViewModel.newDataAnalisis.observe(viewLifecycleOwner){
            it?.let{
                newAngle(it)
            }
        }
    }

    private fun setupListener(){

        binding.switchAutoscale.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(),"Autoescale: $isChecked", Toast.LENGTH_LONG).show()

            setAutoScale(isChecked)
        }

        binding.rgSizeSamples.setOnCheckedChangeListener { _, checkedId ->
            frameSize = when (checkedId) {
                R.id.rb_samples_10 -> 10
                R.id.rb_samples_100 -> 100
                R.id.rb_samples_1000 -> 1000
                else -> 10
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

        binding.btnGenerateDataset.setOnClickListener {
            for (i in 0..100){
                val randomData = MainBoardResponse(
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    ((Math.random() -0.5)* 180).toFloat(),
                    ((Math.random() -0.5)* 180).toFloat(),
                    ((Math.random() -0.5)* 180).toFloat(),
                    0f,
                    40,
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                    Math.random().toInt().toShort(),
                )
                newAngle(randomData)
            }
        }
    }

    private fun initGraph(){

        chart = binding.chart1

        chart.setOnChartValueSelectedListener(this)

        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false                             // Configura el eje
        chart.setDrawBorders(true)

        chart.axisLeft.isEnabled = true
        chart.axisLeft.setDrawAxisLine(true)
        chart.axisLeft.setDrawGridLines(true)

        chart.axisRight.isEnabled = true
        chart.axisRight.setDrawAxisLine(true)
        chart.axisRight.setDrawGridLines(true)

        setAutoScale(binding.switchAutoscale.isChecked)

        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.setDrawGridLines(true)

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        chart.legend.orientation = Legend.LegendOrientation.VERTICAL

        chart.legend.form = Legend.LegendForm.CIRCLE
        chart.legend.setDrawInside(true)

        entryAnglePitch = ArrayList()
        entryAngleRoll = ArrayList()
        entryAngleYaw = ArrayList()
        entryMotorL = ArrayList()
        entryMotorR = ArrayList()

        initTimeStamp =  System.currentTimeMillis()
    }

    private fun setAutoScale( autoScale : Boolean ){

        if( !autoScale ) {
            chart.axisLeft.axisMinimum = -actualLimitScale
            chart.axisLeft.axisMaximum = actualLimitScale
            chart.axisRight.axisMinimum = -actualLimitScale
            chart.axisRight.axisMaximum = actualLimitScale
        }
        else{
            chart.axisLeft.resetAxisMaximum()
            chart.axisLeft.resetAxisMinimum()
            chart.axisRight.resetAxisMaximum()
            chart.axisRight.resetAxisMinimum()
        }
    }

    private fun newAngle( newFrame : MainBoardResponse){

        val actualTimeInSec = ((System.currentTimeMillis() - initTimeStamp).toFloat())/1000

        entryAnglePitch?.add(Entry(actualTimeInSec,newFrame.pitchAngle))                                    // Agrego nuevo punto
        entryAngleRoll?.add(Entry(actualTimeInSec,newFrame.rollAngle))                                      // Agrego nuevo punto
        entryAngleYaw?.add(Entry(actualTimeInSec,newFrame.yawAngle))                                        // Agrego nuevo punto

        lineDataAnglePitch = LineDataSet(entryAnglePitch?.takeLast(frameSize), "Pitch Angle")          // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label
        lineDataAngleRoll = LineDataSet(entryAngleRoll?.takeLast(frameSize), "Roll Angle")             // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label
        lineDataAngleYaw = LineDataSet(entryAngleYaw?.takeLast(frameSize), "Yaw Angle")                // Agrego nuevo dataset que es el conjunto de puntos relacionados entre si, con su label

        entryMotorL?.add(Entry(actualTimeInSec,newFrame.speedL.toFloat()))
        lineDataMotorL= LineDataSet(entryMotorL?.takeLast(frameSize), "Motor L")
        entryMotorR?.add(Entry(actualTimeInSec,newFrame.speedR.toFloat()))
        lineDataMotorR= LineDataSet(entryMotorR?.takeLast(frameSize), "Motor R")

        lineDataAnglePitch?.let {
            it.lineWidth = 2.5f
            it.circleRadius = 4f
            val color = requireContext().getColor(R.color.red_80_percent)
            it.color = color
            it.setCircleColor(color)
        }

        lineDataAngleRoll?.let {
            it.lineWidth = 2.5f
            it.circleRadius = 4f
            val color = requireContext().getColor(R.color.green_80_percent)
            it.color = color
            it.setCircleColor(color)
        }

        lineDataAngleYaw?.let {
            it.lineWidth = 2.5f
            it.circleRadius = 4f
            val color = requireContext().getColor(R.color.yellow_80_percent)
            it.color = color
            it.setCircleColor(color)
        }

        lineDataMotorL?.let {
            it.lineWidth = 2.5f
            it.circleRadius = 4f
            val color = requireContext().getColor(R.color.blue_80_percent)
            it.color = color
            it.setCircleColor(color)
        }

        lineDataMotorR?.let {
            it.lineWidth = 2.5f
            it.circleRadius = 4f
            val color = requireContext().getColor(R.color.red_80_percent)
            it.color = color
            it.setCircleColor(color)
        }

        dataSet = when(viewDataset){
            DatasetView.DATASET_IMU -> {
                arrayImuDataset.clear()
                arrayImuDataset.add(lineDataAnglePitch!!)
                arrayImuDataset.add(lineDataAngleRoll!!)
//                arrayImuDataset.add(lineDataAngleYaw!!)

                setImuMode( newFrame )
                LineData(arrayImuDataset)

            }
            DatasetView.DATASET_MOTOR -> {

                arrayMotorDataset.clear()
                arrayMotorDataset.add(lineDataMotorL!!)
                arrayMotorDataset.add(lineDataMotorR!!)

                setMotorControlMode()
                LineData(arrayMotorDataset)
            }
//            DatasetView.DATASET_PID -> {
//
//            }
            else -> {
                null
            }
        }

        chart.data = dataSet
        chart.invalidate()
    }


    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("VAL SELECTED", "Value: " + e?.y + ", xIndex: " + e?.x + ", DataSet index: " + h?.dataSetIndex)
    }

    override fun onNothingSelected() {

    }

    private fun setMotorControlMode( ) {
        actualLimitScale = 1200F
        setAutoScale(binding.switchAutoscale.isChecked)
        chart.axisLeft.removeAllLimitLines()
    }

    private fun setImuMode( actualLineLimits : MainBoardResponse){

        actualLimitScale = 100F
        setAutoScale(binding.switchAutoscale.isChecked)

        chart.axisLeft.removeAllLimitLines()
        val colorSafetyLimits = requireContext().getColor(androidx.appcompat.R.color.material_deep_teal_200)

        val centerAngleLimitLine = LimitLine(actualLineLimits.centerAngle, "Centro de gravedad")
        centerAngleLimitLine.lineWidth = 2f
        centerAngleLimitLine.enableDashedLine(20f, 10f, 10f)
        centerAngleLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        centerAngleLimitLine.textSize = 10f
        centerAngleLimitLine.lineColor = requireContext().getColor(R.color.status_blue)
        chart.axisLeft.addLimitLine(centerAngleLimitLine)

        val upperLimitLine = LimitLine(actualLineLimits.centerAngle + actualLineLimits.safetyLimits, "Limite seguridad superior")
        upperLimitLine.lineWidth = 2f
        upperLimitLine.enableDashedLine(20f, 10f, 10f)
        upperLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        upperLimitLine.textSize = 10f
        upperLimitLine.lineColor = colorSafetyLimits
        chart.axisLeft.addLimitLine(upperLimitLine)

        val lowerLimitLine = LimitLine(actualLineLimits.centerAngle - actualLineLimits.safetyLimits, "Limite seguridad inferior")
        lowerLimitLine.lineWidth = 2f
        lowerLimitLine.enableDashedLine(20f, 10f, 10f)
        lowerLimitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        lowerLimitLine.textSize = 10f
        lowerLimitLine.lineColor = colorSafetyLimits
        chart.axisLeft.addLimitLine(lowerLimitLine)
    }
}


enum class DatasetView{
    DATASET_IMU,
    DATASET_MOTOR,
    DATASET_PID
}