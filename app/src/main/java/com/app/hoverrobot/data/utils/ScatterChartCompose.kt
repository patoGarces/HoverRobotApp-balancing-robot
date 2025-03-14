package com.app.hoverrobot.data.utils

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.app.hoverrobot.data.models.comms.PointCloudItem
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.Collections

@Composable
fun ScatterChartCompose(
    newPointCloudItem: State<PointCloudItem?>,
) {
    val entries = remember { mutableListOf<Entry>() }

    LaunchedEffect(newPointCloudItem.value) {
        newPointCloudItem.value?.let { newPoint ->
            entries.add(Entry(newPoint.x, newPoint.y))
            Collections.sort(entries, EntryXComparator())   // Si no se ordena rompe el grafico y la app
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            ScatterChart(context).apply {
                setTouchEnabled(false)
                description.isEnabled = false
                legend.isEnabled = false
                invalidate()
            }
        },
        update = { lineChart ->

            val dataSet = ScatterDataSet(entries, "").apply {
                color = Color.RED
                scatterShapeSize = 10f
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            }

            lineChart.data = ScatterData(dataSet)
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }
    )
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun ScatterChartComposePreview() {
    val dummyPointCloudItem = remember { mutableStateOf(PointCloudItem()) }

    ScatterChartCompose(dummyPointCloudItem)
}