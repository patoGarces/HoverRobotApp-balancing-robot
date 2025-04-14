package com.app.hoverrobot.ui.screens.navigationScreen

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.app.hoverrobot.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.ui.composeUtils.ScatterChartCompose
import com.app.hoverrobot.ui.composeUtils.ArcSeekBar
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.DistancePickerDialog
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawLeftAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnYawRightAction
import com.app.hoverrobot.ui.screens.navigationScreen.compose.CompassComposable
import com.app.hoverrobot.ui.screens.navigationScreen.compose.FixedDirection
import com.app.hoverrobot.ui.screens.navigationScreen.compose.JoystickAnalogCompose
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun NavigationScreen(
    isRobotStabilized: State<Boolean>,
    isRobotConnected: State<Boolean>,
    newPointCloudItem: State<PointCloudItem?>,
    actualDegress: State<Int>,
    enableSeekbarsYaw: Boolean = false,
    disableCompass: Boolean = false,
    onActionScreen: (NavigationScreenAction) -> Unit
) {
    var joystickX by remember { mutableIntStateOf(0) }
    var joystickY by remember { mutableIntStateOf(0) }
    var leftAngleDir by remember { mutableIntStateOf(1) }
    var rightAngleDir by remember { mutableIntStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    var isForwardMove by remember { mutableStateOf(true) }

    LaunchedEffect(isRobotStabilized) {
        while (isRobotStabilized.value) {
            onActionScreen(NavigationScreenAction.OnNewJoystickInteraction(joystickX, joystickY))
            delay(50)
        }
    }

    if (!isRobotConnected.value) return

    if (showDialog) {
        DistancePickerDialog(
            directionTitle = if (isForwardMove) R.string.title_forward else R.string.title_backward,
            initialDistance = 1F,
            onDismiss = { showDialog = false },
            onConfirm = { meters ->
                showDialog = false
                val dirMeters = if (isForwardMove) meters else -meters
                NavigationScreenAction.OnFixedDistance(dirMeters)
            },
        )
    }

    Box {
        ScatterChartCompose(newPointCloudItem)

        Column(Modifier.align(Alignment.BottomCenter)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(Color.Transparent),
                    onClick = {
                        isForwardMove = false
                        showDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
                ) {
                    Text(
                        text = stringResource(R.string.title_backward),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                OutlinedButton(
                    modifier = Modifier
                        .wrapContentSize()
                        .widthIn(80.dp)
                        .background(Color.Transparent),
                    onClick = {
                        isForwardMove = true
                        showDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
                ) {
                    Text(
                        text = stringResource(R.string.title_forward),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(Modifier.padding(top = 8.dp)) {
                    JoystickAnalogCompose(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .zIndex(1F),
                        size = 100.dp,
                        dotSize = 50.dp,
                        fixedDirection = FixedDirection.VERTICAL
                    ) { x: Float, y: Float ->
                        joystickY = (y * 100F).toInt()
                    }

                    if (enableSeekbarsYaw) {
                        ArcSeekBar(
                            modifier = Modifier.align(Alignment.TopCenter),
                            range = 1F..180F,
                            sizeArc = 140.dp
                        ) { leftAngleDir = -it.toInt() }
                    }
                }

                Column(
                    Modifier
                        .padding(horizontal = 32.dp)
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YawControlButtons(
                        isRobotStabilized = isRobotStabilized.value,
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .zIndex(1F),
                        size = 100.dp,
                        dotSize = 50.dp,
                        fixedDirection = FixedDirection.HORIZONTAL
                    ) { x: Float, y: Float ->
                        joystickX = (x * 100F).toInt()
                    }

                    if (enableSeekbarsYaw) {
                        ArcSeekBar(
                            modifier = Modifier.align(Alignment.TopCenter),
                            range = 1F..180F,
                            sizeArc = 140.dp
                        ) { rightAngleDir = it.toInt() }
                    }
                }
            }

            // TODO: revisar por que al mostrar este composable se rompe la preview
            if (!disableCompass) {
                CompassComposable(actualDegress) {
                    onActionScreen(OnNewDragCompassInteraction(it))
                }
            }
        }
    }
}

@Composable
private fun YawControlButtons(
    isRobotStabilized: Boolean,
    enableSeekbarsYaw: Boolean = false,
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
        if (enableSeekbarsYaw) {
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

        if (enableSeekbarsYaw) {
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
}

@Composable
@CustomPreview
fun NavigationButtonPreview() {
    val dummySetDegress = remember { mutableIntStateOf(0) }
    val dummyPointCloudItem = remember { mutableStateOf(PointCloudItem()) }
    val dummyRobotConnected = remember { mutableStateOf(true) }
    val dummyRobotStabilized = remember { mutableStateOf(true) }

    MyAppTheme {
        NavigationScreen(
            actualDegress = dummySetDegress,
            newPointCloudItem = dummyPointCloudItem,
            isRobotConnected = dummyRobotConnected,
            isRobotStabilized = dummyRobotStabilized,
            disableCompass = true
        ) { }
    }
}