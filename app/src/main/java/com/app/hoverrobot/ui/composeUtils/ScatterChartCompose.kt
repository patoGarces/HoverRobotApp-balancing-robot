package com.app.hoverrobot.ui.composeUtils

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
import com.github.mikephil.charting.components.XAxis
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

                xAxis.position = XAxis.XAxisPosition.BOTTOM // Asegura que el eje X esté en la parte inferior
                xAxis.setDrawAxisLine(true) // Activa la línea del eje X

                // ocultar las líneas de los ejes
                axisLeft.setDrawAxisLine(false)  // Elimina línea izquierda (Y)
                axisRight.setDrawAxisLine(false) // Elimina línea derecha (Y)
                xAxis.setDrawAxisLine(false)     // Elimina línea del eje X

                // ocultar las líneas de la cuadrícula
                axisLeft.setDrawGridLines(false)
                axisRight.setDrawGridLines(false)
                xAxis.setDrawGridLines(false)

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

@Composable
@CustomPreview
private fun ScatterChartComposePreview() {
    val dummyPointCloudItem = remember { mutableStateOf(PointCloudItem()) }

    ScatterChartCompose(dummyPointCloudItem)
}