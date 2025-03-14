package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun ClosedFloatingPointRange<Float>.toAngle(value: Float): Float =
    180f + (value * 180f)

fun ClosedFloatingPointRange<Float>.toValue(angle: Float): Float =
    ((angle - 180f) / 180f).coerceIn(0F..1F) * this.endInclusive

@Composable
fun ArcSeekBar(
    modifier: Modifier = Modifier,
    sizeArc: Dp,
    range: ClosedFloatingPointRange<Float> = 0F..1F,
    strokeWidth: Float = 20F,
    thumbSize: Float = 50f,
    thumbColor: Color = Color.Red,
    trackColor: Color = Color.Gray,
    activeTrackColor: Color = Color.Red,
    onValueChange: (Float) -> Unit
) {
    val normalizeRange = 0F..1F
    var progress by remember { mutableFloatStateOf(0F) }
    var thumbAngle by remember { mutableFloatStateOf(normalizeRange.toAngle(progress)) }

    Canvas(
        modifier = modifier
            .size(sizeArc)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val touchX = change.position.x
                    val touchY = change.position.y
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val angle = atan2(centerY - touchY, centerX - touchX) * (180f / Math.PI.toFloat()) + 180f

                    if (angle in 180f..360f) {
                        thumbAngle = angle
                        progress = range.toValue(angle)
                        onValueChange(progress)
                    }
                }
            }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2// + size.height / 4
        val arcRadius = size.width / 2 - strokeWidth / 2
        
        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = Size(arcRadius * 2, arcRadius * 2),
            topLeft = Offset(centerX - arcRadius, centerY - arcRadius)
        )

        drawArc(
            color = activeTrackColor,
            startAngle = 180f,
            sweepAngle = thumbAngle - 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = Size(arcRadius * 2, arcRadius * 2),
            topLeft = Offset(centerX - arcRadius, centerY - arcRadius)
        )

        val thumbX = centerX + arcRadius * cos(thumbAngle * (Math.PI / 180)).toFloat()
        val thumbY = centerY + arcRadius * sin(thumbAngle * (Math.PI / 180)).toFloat()

        drawCircle(
            color = thumbColor,
            radius = thumbSize/2,
            center = Offset(thumbX, thumbY)
        )
    }
}

@Preview
@Composable
private fun ArcSeekBarPreview() {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ArcSeekBar(sizeArc = 200.dp) { }
    }
}