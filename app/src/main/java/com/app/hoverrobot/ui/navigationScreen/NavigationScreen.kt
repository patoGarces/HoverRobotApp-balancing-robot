package com.app.hoverrobot.ui.navigationScreen

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.data.utils.ToolBox.round
import com.app.hoverrobot.ui.RobotStateViewModel
import com.app.hoverrobot.ui.composeUtils.ScatterChartCompose
import com.app.hoverrobot.ui.composeUtils.ArcSeekBar
import com.app.hoverrobot.ui.composeUtils.DistancePickerDialog
import com.app.hoverrobot.ui.navigationScreen.compose.CompassComposable
import com.app.hoverrobot.ui.navigationScreen.compose.FixedDirection
import com.app.hoverrobot.ui.navigationScreen.compose.JoystickAnalogCompose
import kotlin.math.abs

@Composable
fun NavigationScreen(
    robotStateViewModel: RobotStateViewModel,
    enableSeekbarsYaw: Boolean = false,
    disableCompass: Boolean = false,
) {
    var joystickX by remember { mutableIntStateOf(0) }
    var joystickY by remember { mutableIntStateOf(0) }
    var leftAngleDir by remember { mutableIntStateOf(1) }
    var rightAngleDir by remember { mutableIntStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    var isForwardMove by remember { mutableStateOf(true) }
    var actualDegress = remember {
        derivedStateOf { robotStateViewModel.robotDynamicData?.yawAngle?.toInt() ?: 0 }
    }

    LaunchedEffect(robotStateViewModel.isRobotStabilized) {
        while (robotStateViewModel.isRobotStabilized) {
            robotStateViewModel.newCoordinatesJoystick(
                (joystickX * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round().toInt(),
                (joystickY * robotStateViewModel.getAggressivenessLevel().normalizedFactor).round().toInt()
            )
            delay(50)
        }
    }

    if (!robotStateViewModel.isRobotConnected) return

    if (showDialog) {
        DistancePickerDialog(
            directionTitle = if (isForwardMove) R.string.title_forward else R.string.title_backward,
            initialDistance = 1F,
            onDismiss = { showDialog = false },
            onConfirm = { meters ->
                showDialog = false
                val dirMeters = if (isForwardMove) meters else -meters

                robotStateViewModel.sendNewMovePosition(
                    abs(dirMeters),
                    dirMeters < 0
                )
            },
        )
    }

    Box {
        ScatterChartCompose(robotStateViewModel.pointCloud)

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
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = stringResource(R.string.title_backward),
                        color = Color.White
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
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = stringResource(R.string.title_forward),
                        color = Color.White
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
                        isRobotStabilized = robotStateViewModel.isRobotStabilized,
                        yawLeftText = stringResource(
                            R.string.control_move_placeholder_direction,
                            leftAngleDir
                        ),
                        yawLeftOnClick = {
                            robotStateViewModel.sendNewMoveRelYaw(leftAngleDir.toFloat())
                        },
                        yawRightText = stringResource(
                            R.string.control_move_placeholder_direction,
                            rightAngleDir
                        ),
                        yawRightOnClick = {
                            robotStateViewModel.sendNewMoveRelYaw(rightAngleDir.toFloat())
                        },
                        onDearmedClick = { robotStateViewModel.sendDearmedCommand() }
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
                CompassComposable(actualDegress = actualDegress) {
                    robotStateViewModel.sendNewMoveAbsYaw(it)
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
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
fun NavigationButtonPreview() {
    val dummySetDegress = remember { mutableIntStateOf(0) }
    val dummyPointCloudItem = remember { mutableStateOf(PointCloudItem()) }
    Column(
        Modifier
            .padding(16.dp)
            .background(Color.Black)
    ) {
//        NavigationScreen(
//            newDegress = dummySetDegress,
//            newPointCloudItem = dummyPointCloudItem,
//            isRobotConnected = true,
//            isRobotStabilized = true,
//            disableCompass = true
//        ) { }
    }
}