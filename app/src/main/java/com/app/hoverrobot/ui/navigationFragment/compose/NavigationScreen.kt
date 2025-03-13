package com.app.hoverrobot.ui.navigationFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnNewJoystickInteraction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.navigationFragment.compose.NavigationScreenAction.OnYawRightAction
import kotlinx.coroutines.delay
import androidx.compose.runtime.State


sealed class NavigationScreenAction {
    data class OnYawLeftAction(val relativeYaw: Int): NavigationScreenAction()
    data class OnYawRightAction(val relativeYaw: Int): NavigationScreenAction()
    data object OnDearmedAction: NavigationScreenAction()
    data class OnNewDragCompassInteraction(val newDegress: Float): NavigationScreenAction()
    data class OnNewJoystickInteraction(val x: Float, val y: Float): NavigationScreenAction()
}

@Composable
fun NavigationScreen(
    isRobotStabilized: Boolean,
    newDegress: State<Int>,
    onActionScreen: (NavigationScreenAction) -> Unit
) {
    var joystickX by remember { mutableFloatStateOf(0F) }
    var joystickY by remember { mutableFloatStateOf(0F) }
    var leftAngleDir by remember { mutableIntStateOf(1) }
    var rightAngleDir by remember { mutableIntStateOf(1) }

    LaunchedEffect(isRobotStabilized) {
        while (isRobotStabilized) {
            onActionScreen(OnNewJoystickInteraction(joystickX, joystickY))
            delay(50)
        }
    }

    Column {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(Modifier.padding(top = 8.dp)) {
                JoystickAnalogCompose(
                    modifier = Modifier.align(Alignment.Center).zIndex(1F),
                    size = 100.dp,
                    dotSize = 50.dp,
                    fixedDirection = FixedDirection.VERTICAL
                ) { x: Float, y: Float ->
                    joystickY = y
                }

                ArcSeekBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    range = 1F..180F,
                    sizeArc = 140.dp
                ) { leftAngleDir = -it.toInt() }
            }

            Column(
                Modifier
                    .padding(horizontal = 32.dp)
                    .weight(1F),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                YawControlButtons(
                    isRobotStabilized = isRobotStabilized,
                    yawLeftText = stringResource(
                        R.string.control_move_placeholder_direction,
                        leftAngleDir
                    ),
                    yawLeftOnClick = {
                        onActionScreen(OnYawLeftAction(leftAngleDir))
                    },
                    yawRightText = stringResource(
                        R.string.control_move_placeholder_direction,
                        rightAngleDir
                    ),
                    yawRightOnClick = {
                        onActionScreen(OnYawRightAction(rightAngleDir))
                    },
                    onDearmedClick = { onActionScreen(OnDearmedAction) }
                )
            }

            Box(Modifier.padding(top = 8.dp)) {

                JoystickAnalogCompose(
                    modifier = Modifier.align(Alignment.Center).zIndex(1F),
                    size = 100.dp,
                    dotSize = 50.dp,
                    fixedDirection = FixedDirection.HORIZONTAL
                ) { x: Float, y: Float ->
                    joystickX = x
                }

                ArcSeekBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    range = 1F..180F,
                    sizeArc = 140.dp
                ) { rightAngleDir = it.toInt() }
            }
        }

        CompassComposable(newDegress) {
            onActionScreen(OnNewDragCompassInteraction(it))
        }
    }
}

@Composable
private fun YawControlButtons(
    isRobotStabilized: Boolean,
    yawLeftText: String,
    yawLeftOnClick: () -> Unit,
    yawRightText: String,
    yawRightOnClick: () -> Unit,
    onDearmedClick: () -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            modifier = Modifier
                .wrapContentSize()
                .widthIn(80.dp)
                .background(Color.Transparent),
            onClick = yawLeftOnClick,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Text(
                text = yawLeftText,
                color = Color.White
            )
        }

        if (isRobotStabilized) {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .background(Color.Transparent)
                    .weight(1F),
                onClick = onDearmedClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF009688))
            ) {
                Text(
                    text = stringResource(id = R.string.dearmed_button),
                    color = Color.White
                )
            }
        }

        OutlinedButton(
            modifier = Modifier
                .wrapContentSize()
                .widthIn(80.dp)
                .background(Color.Transparent),
            onClick = yawRightOnClick,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Text(
                text = yawRightText,
                color = Color.White
            )
        }
    }
}

@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
fun NavigationButtonPreview() {
    val dummySetDegress = remember { mutableIntStateOf(0) }
    Column(
        Modifier
            .padding(16.dp)
            .background(Color.Black)
    ) {
        NavigationScreen(
            newDegress = dummySetDegress,
            isRobotStabilized = true,
        ) { }
    }
}