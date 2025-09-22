package com.app.hoverrobot.ui.screens.navigationScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.app.hoverrobot.data.models.comms.CollisionSensors
import com.app.hoverrobot.ui.composeUtils.CustomColors
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Bold
import com.app.hoverrobot.ui.composeUtils.DistancePickerDialog
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnDearmedAction
import com.app.hoverrobot.ui.screens.navigationScreen.NavigationScreenAction.OnNewDragCompassInteraction
import com.app.hoverrobot.ui.screens.navigationScreen.compose.CompassComposable
import com.app.hoverrobot.ui.screens.navigationScreen.compose.FixedDirection
import com.app.hoverrobot.ui.screens.navigationScreen.compose.JoystickAnalogCompose
import com.app.hoverrobot.ui.theme.MyAppTheme

const val MAX_LINEAR_VELOCITY_MPS   = 1.0F      // m/s
const val MAX_ANGULAR_VELOCITY_RPS  = 3.0F      // rad/s

@Composable
fun NavigationScreen(
    isRobotStabilized: State<Boolean>,
    isRobotConnected: State<Boolean>,
    newPointCloudItem: State<PointCloudItem?>,
    actualDegrees: State<Int>,
    disableCompass: Boolean = false,
    distanceSensors: CollisionSensors,
    onActionScreen: (NavigationScreenAction) -> Unit
) {
    var joystickX by remember { mutableIntStateOf(0) }
    var joystickY by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var isForwardMove by remember { mutableStateOf(true) }

    val pointList = remember { mutableStateListOf<PointCloudItem>() }

    LaunchedEffect(newPointCloudItem.value) {
        newPointCloudItem.value?.let { newPoint ->
            pointList.add(newPoint)
        }
    }

    LaunchedEffect(joystickX, joystickY) {
        onActionScreen(NavigationScreenAction.OnNewJoystickInteraction(joystickX, joystickY))
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
//        ScatterChartCompose(newPointCloudItem)

        TrajectoryMap(
            points = pointList.map { Offset(it.x, it.y) },
            modifier = Modifier.fillMaxSize()
        )

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
                    joystickY = (y * 100F * MAX_LINEAR_VELOCITY_MPS).toInt()
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
                    joystickX = (x * 100F * MAX_ANGULAR_VELOCITY_RPS).toInt()
                }
            }

            // TODO: revisar por que al mostrar este composable se rompe la preview
            if (!disableCompass) {
                CompassComposable(actualDegrees) {
                    onActionScreen(OnNewDragCompassInteraction(it))
                }
            }
        }

        DistanceSensors(modifier = Modifier.align(Alignment.BottomCenter),distanceSensors)
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

/*
@Composable
fun TrajectoryMap(
    points: List<Offset>,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(200f) } // Aumentamos la escala
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offset += pan
                    scale *= zoom
                }
            }
            .background(Color.Blue)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = size.center // 📍 Centro del canvas (x: mitad ancho, y: mitad alto)

            withTransform({
                translate(canvasCenter.x + offset.x, canvasCenter.y + offset.y)
                scale(scale, -scale) // 🔁 Negativo para invertir eje Y (hacia arriba)
            }) {
                for (point in points) {
                    drawCircle(
                        color = Color.Green,
                        radius = 10f, // Aumentamos el tamaño del punto
                        center = point
                    )
                }
            }
        }
    }
}

 */
@Composable
fun TrajectoryMap(
    points: List<Offset>,
    modifier: Modifier = Modifier
) {
    val zoomFactor = 100f // 1 metro = 50 pixeles
    val dotColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = size.center // El centro del canvas

            // Dibujar líneas de trayectoria
            if (points.size > 1) {
                for (i in 0 until points.size - 1) {
                    val start = canvasCenter + points[i] * zoomFactor
                    val end = canvasCenter + points[i + 1] * zoomFactor

                    drawLine(
                        color = Color.Cyan,
                        start = start,
                        end = end,
                        strokeWidth = 2f
                    )
                }
            }

            // Dibujar puntos
            for (point in points) {
                val pointPos = canvasCenter + point * zoomFactor // Aplica el zoom a las coordenadas
                drawCircle(
                    color = dotColor,
                    radius = 5f, // Ajuste el tamaño del punto
                    center = pointPos
                )
            }
        }
    }
}

private val Float?.toFormatStringDistance: String
    @Composable
    get() = this?.let {
        stringResource(R.string.placeholder_distances, this)
    } ?: stringResource(R.string.unknown_distance)

private val Float?.toFormatColorDistance: Color
    @Composable
    get() = if ( this!= null && this < 30) {
        CustomColors.StatusRed
    } else MaterialTheme.colorScheme.onBackground

@Composable
private fun DistanceSensors(
    modifier: Modifier,
    distances: CollisionSensors
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            modifier = Modifier.weight(1F),
            text = distances.sensorFrontLeft.toFormatStringDistance,
            style = textStyle14Bold,
            color = distances.sensorFrontLeft.toFormatColorDistance
        )

        Text(
            modifier = Modifier.weight(1F),
            text = distances.sensorRearLeft.toFormatStringDistance,
            style = textStyle14Bold,
            color = distances.sensorRearLeft.toFormatColorDistance
        )

        Text(
            modifier = Modifier.weight(1F),
            text = distances.sensorRearRight.toFormatStringDistance,
            style = textStyle14Bold,
            color = distances.sensorRearRight.toFormatColorDistance
        )

        Text(
            modifier = Modifier.weight(1F),
            text = distances.sensorFrontRight.toFormatStringDistance,
            style = textStyle14Bold,
            color = distances.sensorFrontRight.toFormatColorDistance
        )
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
            distanceSensors = CollisionSensors(),
            disableCompass = true
        ) { }
    }
}