package com.app.hoverrobot.ui.navigationFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreen
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnNewJoystickInteraction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnYawRightAction
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterDataSet
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

class NavigationFragment : Fragment() {

    private val navigationViewModel: NavigationViewModel by viewModels(ownerProducer = { requireActivity() })

    private var entryDataPoints: ArrayList<Entry> = ArrayList()
    private lateinit var scatterDataset: ScatterDataSet

    private val dualRateAggressiveness: Float
        get() = AggressivenessLevels.getLevelPercent(
            navigationViewModel.aggressivenessLevel.value ?: 0
        ) ?: 0F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val actualYawAngle = navigationViewModel.dynamicData
                .map { it.yawAngle.toInt() }
                .observeAsState(initial = 0)

            NavigationScreen(
                isRobotStabilized = navigationViewModel.isRobotStabilized.collectAsState().value,
                newDegress = actualYawAngle
            ) { onAction ->
                when (onAction) {
                    is OnDearmedAction -> navigationViewModel.sendDearmedCommand()
                    is OnYawLeftAction -> navigationViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
                    is OnYawRightAction -> navigationViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
                    is OnNewDragCompassInteraction -> navigationViewModel.sendNewMoveAbsYaw(onAction.newDegress)
                    is NavigationScreenAction.OnFixedDistance -> {
                        Log.d("DistanceFixed",onAction.meters.toString())
                        navigationViewModel.sendNewMovePosition(
                            abs(onAction.meters),
                            onAction.meters < 0
                        )
                    }

                    is OnNewJoystickInteraction -> {
                        val dualRateX = onAction.x * dualRateAggressiveness.toDouble()
                        val dualRateY = onAction.y * dualRateAggressiveness.toDouble()

                        Log.i(
                            "JoystickCompose",
                            "${dualRateX.round().toInt()}, ${dualRateY.round().toInt()}"
                        )
                        navigationViewModel.newCoordinatesJoystick(
                            dualRateX.round().toInt(),
                            dualRateY.round().toInt()
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
//        initGraph()
    }

    private fun setupObserver() {
        navigationViewModel.joyVisible.observe(viewLifecycleOwner) {
//            binding.navigationVisible.isVisible = it ?: false
        }

//        navigationViewModel.pointCloud.observe(viewLifecycleOwner) {
//
//            Log.d(TAG,"Nuevo punto: [${it.last().x},${it.last().y}]")
//            entryDataPoints.add(Entry(it.last().x, it.last().y))
//
//            Collections.sort(entryDataPoints, EntryXComparator())
//
//            val dataSet = ScatterDataSet(entryDataPoints, "").apply {
//                color = Color.RED
//                scatterShapeSize = 10f
//                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
//            }
//
//            scatterDataset.notifyDataSetChanged()
//            binding.scatterChart.data = ScatterData(dataSet)
//            binding.scatterChart.notifyDataSetChanged()
//            binding.scatterChart.invalidate()
    }
}

//    private fun initGraph() {
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

//        scatterDataset = ScatterDataSet(entryDataPoints, "").apply {
////            color = Color.BLUE
//            color = Color.RED
//            scatterShapeSize = 10f
//            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
//        }
//
//        binding.scatterChart.setTouchEnabled(false)
//        binding.scatterChart.description.isEnabled = false
//        binding.scatterChart.legend.isEnabled = false
////        binding.scatterChart.data = ScatterData(dataSet)
//        binding.scatterChart.invalidate()
//    }

private fun Float.round(decimals: Int = 2): Float = "%.${decimals}f".format(this).toFloat()
private fun Double.round(decimals: Int = 2): Float =
    BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toFloat() * 100
//}

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