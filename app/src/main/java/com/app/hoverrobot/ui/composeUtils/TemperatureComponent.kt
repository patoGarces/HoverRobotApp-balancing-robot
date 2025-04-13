package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R

@Composable
fun TemperatureComponent(
    @StringRes title: Int,
    temp: Float,
    minTemp: Float = 0f,
    maxTemp: Float = 100f
) {
    val progress = (temp - minTemp) / (maxTemp - minTemp)
    val brush = Brush.sweepGradient(
        colors = listOf(Color.Blue, Color(0xFFFFA500), Color.Red),
    )

    Column(
        Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(title),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            style = CustomTextStyles.textStyle14Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.aspectRatio(1F).fillMaxWidth()) {
                rotate(-90f) {
                    drawArc(
                        brush = brush,
                        startAngle = 10f,
                        sweepAngle = 360 * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            if (temp == 0F) {
                CircularProgressIndicator(
                    Modifier.size(30.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "$temp°",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
@Preview
private fun TemperatureComponentPreview() {

    Column {
        TemperatureComponent(title = R.string.title_mainboard_temp, temp = 5f, 0f, 100f)
        TemperatureComponent(title = R.string.title_motorboard_temp, temp = 32f, 0f, 100f)
        TemperatureComponent(title = R.string.title_imu_temp, temp = 90f, 0f, 100f)
        TemperatureComponent(title = R.string.title_imu_temp, temp = 0F, 0f, 100f)
    }
}