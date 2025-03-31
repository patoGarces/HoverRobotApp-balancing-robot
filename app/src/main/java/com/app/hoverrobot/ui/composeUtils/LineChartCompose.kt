package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.hoverrobot.data.models.ChartLimitsConfig
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun LineChartCompose(
    modifier: Modifier,
    actualLineData: State<LineData?>,
    isAutoScaled: Boolean,
    chartLimitsConfig: State<ChartLimitsConfig>,
    onPause: (Boolean) -> Unit
) {
    var onPauseState by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.border(
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(bottom = 4.dp),
            factory = { context ->
                LineChart(context).apply {

                    // chart
                    setDrawGridBackground(false)
                    description.isEnabled = false
                    setDrawBorders(true)
                    // axis
                    axisLeft.isEnabled = true
                    axisLeft.setDrawAxisLine(true)
                    axisLeft.setDrawGridLines(true)
                    axisRight.isEnabled = true
                    axisRight.setDrawAxisLine(true)
                    axisRight.setDrawGridLines(true)
                    xAxis.setDrawAxisLine(true)
                    xAxis.setDrawGridLines(true)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM

                    // touch gestures
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(false)

                    //legend
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.form = Legend.LegendForm.CIRCLE
                    legend.setDrawInside(false)

                    setDrawMarkers(false)
                    invalidate()
                }
            },
            update = { lineChart ->

                val minAxis = -chartLimitsConfig.value.limitAxis
                val maxAxis = chartLimitsConfig.value.limitAxis

                lineChart.axisLeft.removeAllLimitLines()
                if (!chartLimitsConfig.value.limitLines.isNullOrEmpty()) {
                    chartLimitsConfig.value.limitLines!!.forEach {
                        lineChart.axisLeft.addLimitLine(it)
                    }
                }

                if (!isAutoScaled) {
                    lineChart.axisLeft.axisMinimum = minAxis
                    lineChart.axisLeft.axisMaximum = maxAxis
                    lineChart.axisRight.axisMinimum = minAxis
                    lineChart.axisRight.axisMaximum = maxAxis
                } else {
                    lineChart.axisLeft.resetAxisMaximum()
                    lineChart.axisLeft.resetAxisMinimum()
                    lineChart.axisRight.resetAxisMaximum()
                    lineChart.axisRight.resetAxisMinimum()
                }


                lineChart.data = actualLineData.value
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()
            }
        )

        CustomFloatingButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            icon = if (onPauseState) Icons.Filled.PlayArrow else Icons.Filled.Pause
        ) {
            onPauseState = !onPauseState
            onPause(onPauseState)
        }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun LineChartComposePreview() {

    val dummyEntries = listOf(
        Entry(0f, -90f),
        Entry(1f, 20f),
        Entry(2f, 50f),
        Entry(3f, -60f),
        Entry(4f, 18f)
    )

    val dummyDataSet = LineDataSet(dummyEntries, "Sample Data").apply {
        color = android.graphics.Color.RED
        lineWidth = 2.5f
        circleRadius = 1f
        setDrawCircles(false)
        setDrawCircleHole(false)
        setDrawValues(false)
        setDrawHighlightIndicators(false)
        isHighlightEnabled = false
    }

    val dummyLineData = remember { mutableStateOf(LineData(dummyDataSet)) }
    val dummyLimitAxis = remember { mutableStateOf(ChartLimitsConfig(100F, null)) }

    LineChartCompose(Modifier, dummyLineData, false, dummyLimitAxis) {}
}