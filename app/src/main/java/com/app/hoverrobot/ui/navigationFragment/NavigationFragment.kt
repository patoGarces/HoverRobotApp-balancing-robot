package com.app.hoverrobot.ui.navigationFragment

import android.os.Bundle
import android.view.LayoutInflater
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
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

//class NavigationFragment : Fragment() {
//
//    private val navigationViewModel: NavigationViewModel by viewModels(ownerProducer = { requireActivity() })
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ) = ComposeView(requireContext()).apply {
//        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//        setContent {
//            val actualYawAngle = navigationViewModel.dynamicData
//                .map { it.yawAngle.toInt() }
//                .observeAsState(initial = 0)
//            NavigationScreen(
//                isRobotStabilized = navigationViewModel.isRobotStabilized.collectAsState().value,
//                isRobotConnected = navigationViewModel.isRobotConnected.value,
//                newPointCloudItem = navigationViewModel.pointCloud.observeAsState(null),
//                newDegress = actualYawAngle
//            ) { onAction ->
//                when (onAction) {
//                    is OnDearmedAction -> navigationViewModel.sendDearmedCommand()
//                    is OnYawLeftAction -> navigationViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
//                    is OnYawRightAction -> navigationViewModel.sendNewMoveRelYaw(onAction.relativeYaw.toFloat())
//                    is OnNewDragCompassInteraction -> navigationViewModel.sendNewMoveAbsYaw(onAction.newDegress)
//                    is NavigationScreenAction.OnFixedDistance -> {
//                        navigationViewModel.sendNewMovePosition(
//                            abs(onAction.meters),
//                            onAction.meters < 0
//                        )
//                    }
//
//                    is OnNewJoystickInteraction -> {
//                        navigationViewModel.newCoordinatesJoystick(
//                            (onAction.x * dualRateAggressiveness.toDouble()).round().toInt(),
//                            (onAction.y * dualRateAggressiveness.toDouble()).round().toInt()
//                        )
//                    }
//                }
//            }
//        }
//    }
//}