package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData

@Composable
fun LineChartCompose(
    actualLineData: State<LineData?>,
    isAutoScaled: Boolean,
    limitAxes: State<Float>,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
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
                // touch gestures
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(false)

                //legend
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                legend.orientation = Legend.LegendOrientation.VERTICAL
                legend.form = Legend.LegendForm.LINE
                legend.setDrawInside(true)
                invalidate()
            }
        },
        update = { lineChart ->

            val minAxis = -limitAxes.value
            val maxAxis = limitAxes.value

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

//            val dataSet = LineDataSet(entries, "").apply {
//                color = Color.RED
//                scatterShapeSize = 10f
//                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
//            }

            lineChart.data = actualLineData.value
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }
    )
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun LineChartComposePreview() {
    val dummyLineData = remember { mutableStateOf<LineData?>(null) }
    val dummyLimitAxis = remember { mutableFloatStateOf(100F) }

    LineChartCompose(dummyLineData, false, dummyLimitAxis)
}