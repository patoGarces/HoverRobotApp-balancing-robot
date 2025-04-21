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
    actualDegrees: State<Int>,
    disableCompass: Boolean = false,
    onActionScreen: (NavigationScreenAction) -> Unit
) {
    var joystickX by remember { mutableIntStateOf(0) }
    var joystickY by remember { mutableIntStateOf(0) }
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
                onActionScreen(NavigationScreenAction.OnFixedDistance(dirMeters))
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
                JoystickAnalogCompose(
                    modifier = Modifier.padding(top = 8.dp),
                    size = 100.dp,
                    dotSize = 50.dp,
                    fixedDirection = FixedDirection.VERTICAL
                ) { x: Float, y: Float ->
                    joystickY = (y * 100F).toInt()
                }

                Column(
                    Modifier
                        .padding(horizontal = 32.dp)
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YawControlButtons(
                        isRobotStabilized = isRobotStabilized.value,
                        onDearmedClick = { onActionScreen(OnDearmedAction) }
                    )
                }

                JoystickAnalogCompose(
                    modifier = Modifier.padding(top = 8.dp),
                    size = 100.dp,
                    dotSize = 50.dp,
                    fixedDirection = FixedDirection.HORIZONTAL
                ) { x: Float, y: Float ->
                    joystickX = (x * 100F).toInt()
                }
            }

            // TODO: revisar por que al mostrar este composable se rompe la preview
            if (!disableCompass) {
                CompassComposable(actualDegrees) {
                    onActionScreen(OnNewDragCompassInteraction(it))
                }
            }
        }
    }
}

@Composable
private fun YawControlButtons(
    isRobotStabilized: Boolean,
    onDearmedClick: () -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
                    color = MaterialTheme.colorScheme.onBackground
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
            actualDegrees = dummySetDegress,
            newPointCloudItem = dummyPointCloudItem,
            isRobotConnected = dummyRobotConnected,
            isRobotStabilized = dummyRobotStabilized,
            disableCompass = true
        ) { }
    }
}