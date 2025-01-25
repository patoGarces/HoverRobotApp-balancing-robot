package com.example.hoverrobot.ui.statusDataFragment.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TemperatureComponent(title: String, temp: Float, minTemp: Float = 0f, maxTemp: Float = 100f) {
    val progress = (temp - minTemp) / (maxTemp - minTemp)
    val brush = Brush.sweepGradient(
        colors = listOf(Color.Blue, Color(0xFFFFA500), Color.Red),
    )
    Column(
        Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 14.sp
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
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

            Text(
                text = "$temp°",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
@Preview
private fun TemperatureComponentPreview() {

    Column {
        TemperatureComponent(title = "IMU",temp = 5f,0f,100f)
        TemperatureComponent(title = "MCB",temp = 32f,0f,100f)
        TemperatureComponent(title = "Mainboard",temp = 90f,0f,100f)
    }
}