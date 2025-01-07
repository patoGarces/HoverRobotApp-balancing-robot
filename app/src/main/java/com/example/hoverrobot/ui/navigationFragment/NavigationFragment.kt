package com.example.hoverrobot.ui.navigationFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.DirectionControl
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.databinding.NavigationFragmentBinding
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.marcinmoskala.arcseekbar.ProgressListener;
import java.lang.Math.round
import java.util.Collections

class NavigationFragment : Fragment() {

    private val navigationViewModel : NavigationViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding : NavigationFragmentBinding
    private val binding get() = _binding

    private var directionYaw: Int = 0
    private var joyAxisX: Int = 0
    private var joyAxisY: Int = 0

    private var disableCompassSet = false

    private var jobSendYawCommand: Job? = null

    private var distanceBackward: Float = MIN_DISTANCE
    private var distanceForward: Float = MIN_DISTANCE

    private var entryDataPoints: ArrayList<Entry> = ArrayList()
    private lateinit var scatterDataset: ScatterDataSet

    private val TAG = "ControlFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NavigationFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joystickThrottle.setFixedCenter(true)
        binding.joystickDirection.setFixedCenter(true)
        binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(MIN_DISTANCE)
        binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(MIN_DISTANCE)
        setupListener()
        setupObserver()
        initGraph()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListener(){

        binding.joystickDirection.setOnMoveListener{ _, _ ->
            joyAxisY = 100 - (binding.joystickThrottle.normalizedY * 2)
            joyAxisX = (binding.joystickDirection.normalizedX * 2) -100

            navigationViewModel.newCoordinatesJoystick(DirectionControl(joyAxisX.toShort(),joyAxisY.toShort()))
        }

        binding.joystickThrottle.setOnMoveListener{ _, _ ->
            joyAxisY = 100 - (binding.joystickThrottle.normalizedY * 2)
            joyAxisX = (binding.joystickDirection.normalizedX * 2) -100
            navigationViewModel.newCoordinatesJoystick(DirectionControl(joyAxisX.toShort(),joyAxisY.toShort()))
        }

        binding.compassView.setOnCompassDragListener {
            disableCompassSet = true
            jobSendYawCommand?.cancel()
            jobSendYawCommand = ioScope.launch {
                navigationViewModel.sendNewMoveYaw(it)
                delay(200)
                disableCompassSet = false
           }
        }

//        binding.compassView.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    Log.d("compassView","actionDown")
//                    disableCompassSet = true
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    Log.d("compassView","actionUp")
//                    disableCompassSet = false
//                }
//            }
//            true
//        }

        binding.seekbarBackward.onProgressChangedListener =
            ProgressListener { progress ->
                distanceBackward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
                binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(distanceBackward)
            }

        binding.seekbarForward.onProgressChangedListener =
            ProgressListener { progress ->
                distanceForward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
                binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(distanceForward)
            }

        binding.btnForward.setOnClickListener {
            navigationViewModel.sendNewMovePosition(distanceForward)
        }

        binding.btnBackward.setOnClickListener {
            navigationViewModel.sendNewMovePosition(distanceBackward,true)
        }

        binding.btnYawLeft.setOnClickListener {
            navigationViewModel.sendNewMoveYaw(175f)
        }

        binding.btnYawRight.setOnClickListener {
            navigationViewModel.sendNewMoveYaw(185f)
        }
    }

    private fun setupObserver(){
        navigationViewModel.joyVisible.observe(viewLifecycleOwner) {
            binding.navigationVisible.isVisible = it ?: false
        }

        navigationViewModel.dynamicData.observe(viewLifecycleOwner) {
            if(!disableCompassSet) binding.compassView.degrees = it.yawAngle.toInt().toFloat()
        }

        navigationViewModel.pointCloud.observe(viewLifecycleOwner) {

            Log.d(TAG,"Nuevo punto: [${it.last().x},${it.last().y}]")
            entryDataPoints.add(Entry(it.last().x, it.last().y))

            Collections.sort(entryDataPoints, EntryXComparator())

            val dataSet = ScatterDataSet(entryDataPoints, "").apply {
//            color = Color.BLUE
                color = Color.RED
                scatterShapeSize = 10f
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            }

            scatterDataset.notifyDataSetChanged()
            binding.scatterChart.data = ScatterData(dataSet)
            binding.scatterChart.notifyDataSetChanged()
            binding.scatterChart.invalidate()
        }
    }

    private fun initGraph() {
//        val entries = mutableListOf<Entry>()

//        for (i in 0..100) {
//            val x = ((Math.random()-0.5) * 10).toFloat()
//            val y = ((Math.random()-0.5) * 10).toFloat()
//            entries.add(Entry(x, y))
//        }

//        val dataSet = ScatterDataSet(entries, "").apply {
////            color = Color.BLUE
//            color = Color.RED
//            scatterShapeSize = 10f
//            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
//        }

        scatterDataset = ScatterDataSet(entryDataPoints, "").apply {
//            color = Color.BLUE
            color = Color.RED
            scatterShapeSize = 10f
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        }

        binding.scatterChart.setTouchEnabled(false)
        binding.scatterChart.description.isEnabled = false
        binding.scatterChart.legend.isEnabled = false
//        binding.scatterChart.data = ScatterData(dataSet)
        binding.scatterChart.invalidate()
    }

    private fun Float.round(decimals: Int = 2): Float = "%.${decimals}f".format(this).toFloat()
}

private const val MIN_DISTANCE = 0.1f
private const val MAX_DISTANCE = 1f