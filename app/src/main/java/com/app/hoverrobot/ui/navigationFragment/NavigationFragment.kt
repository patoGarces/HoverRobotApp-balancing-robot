package com.app.hoverrobot.ui.navigationFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.hoverrobot.R
import com.app.hoverrobot.data.utils.ToolBox.ioScope
import com.app.hoverrobot.databinding.NavigationFragmentBinding
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationButtons
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.marcinmoskala.arcseekbar.ProgressListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.sign

class NavigationFragment : Fragment() {

    private val navigationViewModel : NavigationViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding : NavigationFragmentBinding
    private val binding get() = _binding

    private var joyAxisX: Int = 0
    private var joyAxisY: Int = 0

    private var disableCompassSet = false

    private var jobSendYawCommand: Job? = null

    private var distanceBackward: Float = 0.5f
    private var distanceForward: Float = 0.5f

    private var leftDirection: Int = 1
    private var rightDirection: Int = 1

    private var entryDataPoints: ArrayList<Entry> = ArrayList()
    private lateinit var scatterDataset: ScatterDataSet

    private val dualRateAggressiveness: Float
        get() = AggressivenessLevels.getLevelPercent(navigationViewModel.aggressivenessLevel.value ?: 0) ?: 0F

    private val TAG = "ControlFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NavigationFragmentBinding.inflate(inflater,container,false)
        return binding.apply {

            composeView.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                composeView.setContent {
                    NavigationButtons(
                        isRobotStabilized = navigationViewModel.isRobotStabilized.collectAsState().value,
                        yawLeftAngle = "12",
                        yawRightAngle = "34",
                        onYawLeftClick = {},
                        onYawRightClick = {},
                        onDearmedClick = {
                            navigationViewModel.sendDearmedCommand()
                        }
                    )
                }
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joystickThrottle.setFixedCenter(true)
        binding.joystickDirection.setFixedCenter(true)
        binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(0.5f)
        binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(0.5f)
        setupListener()
        setupObserver()
        initGraph()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListener(){

        binding.joystickDirection.setOnMoveListener{ angle, relValue ->
            joyAxisX = (relValue * (90 - angle).sign * dualRateAggressiveness).toInt()
            navigationViewModel.newCoordinatesJoystick(joyAxisX,joyAxisY)
            Log.i("calibrate","Y: $joyAxisX")
        }

        binding.joystickThrottle.setOnMoveListener{ angle, relValue ->
            joyAxisY = (relValue * (180 - angle).sign * dualRateAggressiveness).toInt()
            navigationViewModel.newCoordinatesJoystick(joyAxisX,joyAxisY)
            Log.i("calibrate","Y: $joyAxisY")
        }

        binding.compassView.setOnCompassDragListener {
            disableCompassSet = true
            jobSendYawCommand?.cancel()
            jobSendYawCommand = ioScope.launch {
                navigationViewModel.sendNewMoveAbsYaw(it)
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

        binding.sliderBackward.addOnChangeListener { _,progress,_ ->
            distanceBackward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
            binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(distanceBackward)
        }

        binding.sliderForward.addOnChangeListener { _,progress,_ ->
            distanceForward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
            binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(distanceForward)
        }

        binding.seekbarLeft.onProgressChangedListener =
            ProgressListener { progress ->
                leftDirection = -(MIN_DIR + ((progress / 100f) * (MAX_DIR - MIN_DIR))).toInt()
//                binding.btnYawLeft.text = getString(R.string.control_move_placeholder_direction).format(leftDirection)
            }

        binding.seekbarRight.onProgressChangedListener =
            ProgressListener { progress ->
                rightDirection = (MIN_DIR + ((progress / 100f) * (MAX_DIR - MIN_DIR))).toInt()
//                binding.btnYawRight.text = getString(R.string.control_move_placeholder_direction).format(rightDirection)
            }

        binding.btnForward.setOnClickListener {
            navigationViewModel.sendNewMovePosition(distanceForward)
        }

        binding.btnBackward.setOnClickListener {
            navigationViewModel.sendNewMovePosition(distanceBackward,true)
        }

//        binding.btnYawLeft.setOnClickListener {
//            navigationViewModel.sendNewMoveRelYaw(leftDirection.toFloat())
//        }
//
//        binding.btnYawRight.setOnClickListener {
//            navigationViewModel.sendNewMoveRelYaw(rightDirection.toFloat())
//        }
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

private const val MIN_DIR = 1f
private const val MAX_DIR = 180f

enum class AggressivenessLevels(val normalizePercent: Float) {
    SOFT(0.3F),
    MODERATE(0.6F),
    AGGRESSIVE(1F);

    companion object {
        fun getLevelPercent(indexLevel: Int): Float? {
            return entries.getOrNull(indexLevel)?.normalizePercent
        }
    }
}