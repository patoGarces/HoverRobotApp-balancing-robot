package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(
    modifier: Modifier,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float> = 0F..4F,
    stepSize: Float = 0.01F,
    thumbColor: Color = Color.Red,
    activeTrackColor: Color = Color.Red,
    inactiveTrackColor: Color = Color.Gray,
    onUpdateValue: ((Float) -> Unit)? = null,
    onValueChanged: (Float) -> Unit
) {
    var actualValue by remember { mutableFloatStateOf(initialValue) }

    LaunchedEffect(initialValue) {
        actualValue = initialValue
    }

    Box(modifier) {
        Box(
            modifier = Modifier
                .height(5.dp)
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .background(
                    inactiveTrackColor,
                    RoundedCornerShape(2.dp)
                )
        )

        Box(
            modifier = Modifier
                .height(5.dp)
                .fillMaxWidth(fraction = (actualValue - range.start) / (range.endInclusive - range.start))
                .align(Alignment.CenterStart)
                .background(
                    activeTrackColor,
                    RoundedCornerShape(2.dp)
                )
        )

        Slider(
            value = actualValue,
            onValueChange = {
                actualValue = it
                onUpdateValue?.invoke(it)
            },
            steps = ((range.endInclusive - range.start) / stepSize).toInt() - 1,
            onValueChangeFinished = { onValueChanged(actualValue) },
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            colors = SliderDefaults.colors(
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
            ),
            thumb = {
                Box(
                    Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                        .background(thumbColor, CircleShape)
                )
            }
        )
    }
}

@Preview
@Composable
private fun CustomSliderPreview() {
    Column {
        CustomSlider(
            modifier = Modifier,
            initialValue = 2F
        ) { }

        Spacer(Modifier.height(30.dp))

        CustomSlider(
            modifier = Modifier,
            initialValue = 1F,
            thumbColor = Color.Magenta,
            activeTrackColor = Color.Blue,
            inactiveTrackColor = Color.Black
        ) { }
    }
}