package com.app.hoverrobot.ui.composeUtils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.app.hoverrobot.data.models.comms.PointCloudItem

@Composable
fun ScatterChartCompose(
    newPointCloudItem: State<PointCloudItem?>,
) {

    // TODO: migrar
    /*
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

     */
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun ScatterChartComposePreview() {
    val dummyPointCloudItem = remember { mutableStateOf(PointCloudItem()) }

    ScatterChartCompose(dummyPointCloudItem)
}