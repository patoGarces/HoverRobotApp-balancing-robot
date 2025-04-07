package com.app.hoverrobot.ui.screens.navigationScreen.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.app.hoverrobot.R
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


// Ejemplo a partir de https://github.com/manalkaff/JetStick/tree/main
@Composable
fun JoystickAnalogCompose(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    dotSize: Dp = 50.dp,
    fixedDirection: FixedDirection = FixedDirection.NONE,
    backgroundImage: Int = R.drawable.background_joystick_circle,
    dotImage: Int = R.mipmap.ic_analog_foreground,
    onMoved: (normX: Float, normY: Float) -> Unit = { _, _ -> }
) {
    Box(
        modifier = modifier.size(size)
    ) {
        val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
        val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
        val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

        var offsetX by remember { mutableFloatStateOf(centerX) }
        var offsetY by remember { mutableFloatStateOf(centerY) }

        var radius by remember { mutableFloatStateOf(0f) }
        var theta by remember { mutableFloatStateOf(0f) }

        var positionX by remember { mutableFloatStateOf(0f) }
        var positionY by remember { mutableFloatStateOf(0f) }

        Image(
            painterResource(id = backgroundImage),
            "JoyStickBackground",
            modifier = Modifier.size(size),
        )

        Image(
            painterResource(id = dotImage),
            "JoyStickDot",
            modifier = Modifier
                .offset {
                    IntOffset(
                        (positionX + centerX).roundToInt(),
                        (positionY + centerY).roundToInt()
                    )
                }
                .size(dotSize)
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        offsetX = centerX
                        offsetY = centerY
                        radius = 0f
                        theta = 0f
                        positionX = 0f
                        positionY = 0f
                    }) { pointerInputChange: PointerInputChange, offset: Offset ->
                        val x = offsetX + offset.x - centerX
                        val y = offsetY + offset.y - centerY

                        pointerInputChange.consume()

                        theta = if (x >= 0 && y >= 0) {
                            atan(y / x)
                        } else if (x < 0 && y >= 0) {
                            (Math.PI).toFloat() + atan(y / x)
                        } else if (x < 0 && y < 0) {
                            -(Math.PI).toFloat() + atan(y / x)
                        } else {
                            atan(y / x)
                        }

                        radius = sqrt((x.pow(2)) + (y.pow(2)))

                        offsetX += offset.x
                        offsetY += offset.y

                        val radius2 = if (radius > maxRadius) maxRadius else radius
                        polarToCartesian(radius2, theta).apply {
                            positionX = if(fixedDirection == FixedDirection.VERTICAL) 0F else first
                            positionY = if(fixedDirection == FixedDirection.HORIZONTAL) 0F else second
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    val globalPosX = (coordinates.positionInParent().x - centerX) / maxRadius.toDouble()
                    val globalPosY = -(coordinates.positionInParent().y - centerY) / maxRadius.toDouble()

                    onMoved(
                        BigDecimal(globalPosX).setScale(2, RoundingMode.HALF_UP).toFloat(),
                        BigDecimal(globalPosY).setScale(2, RoundingMode.HALF_UP).toFloat()
                    )
                },
        )
    }
}

private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))

enum class FixedDirection{
    NONE,
    VERTICAL,
    HORIZONTAL
}

@Preview
@Composable
private fun JoystickAnalogComposePreview() {
    JoystickAnalogCompose { x, y -> }
}