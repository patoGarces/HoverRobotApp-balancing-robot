package com.app.hoverrobot.ui.composeUtils

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ir.ehsannarmani.compose_charts.models.ViewRange
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties

@Composable
fun LineChartCompose(
    modifier: Modifier,
    listOfDynamicData: State<List<RobotDynamicData?>>,
    isAutoScaled: Boolean,
    limitAxes: State<Float>,
    onPause: (Boolean) -> Unit
) {
    var onPauseState by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.border(
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(8.dp)
        )
    ) {

        val listOfPointData = remember { mutableStateListOf<Double>() }
        val valuesLineData by remember(listOfPointData) {
            mutableStateOf(listOfPointData)
        }

        LaunchedEffect(listOfDynamicData.value) {
            listOfPointData.clear()
            listOfPointData.addAll(listOfDynamicData.value.mapNotNull { it?.pitchAngle?.toDouble() })

            Log.i("LineChartCompose", "size of listOfDynamicData: ${listOfDynamicData.value.size}, size of listOfPointData: ${listOfPointData.size} ")
        }

//        val valuesLineData = listOfPointData
        val totalDataSize = valuesLineData.size
        val startIndex = maxOf(0, totalDataSize - 10)
        val endIndex = totalDataSize - 1
        Log.i("LineChartCompose","startIndex: $startIndex, endIndex: $endIndex")

//        val data = listOf(28.0, 41.0, 5.0, 10.0, 35.0)

        // Indicadores del eje vertical(Y)
//        val indicatorProperties = HorizontalIndicatorProperties(
//            enabled = true,
//            textStyle = MaterialTheme.typography.labelSmall,
//            count = IndicatorCount.CountBased(count = 5),
//            position = IndicatorPosition.Horizontal.End,
//            padding = 32.dp,
//            contentBuilder = { indicator ->
//                "%.2f".format(indicator) + " Million"
//            },
//            indicators = listOf(10.0,50.0,30.0)
//        )



        LineChart(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            data = remember {
                listOf(
                    Line(
                        label = "Windows",
                        values = valuesLineData,

                        color = SolidColor(Color(0xFF23af92)),
                        curvedEdges = false,
//                        firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
//                        secondGradientFillColor = Color.Transparent,
//                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                        gradientAnimationDelay = 1000,
                        gradientAnimationDelay = 0,
                        strokeAnimationSpec = tween(0),
                        strokeProgress = Animatable(1F),
                        gradientAnimationSpec = tween(0),
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        viewRange = ViewRange(startIndex, endIndex)
                    )
                )
            },
            zeroLineProperties = ZeroLineProperties( enabled =  false ),

            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White)
            ),
            labelHelperPadding = 8.dp,

            // VALORES EN EL EJE X:
//            labelProperties = LabelProperties(
//                enabled = true,
//                textStyle = MaterialTheme.typography.labelSmall,
//                padding = 16.dp,
//                labels = listOf("Apr","Mar"),
//                builder = {modifier,label,shouldRotate,index->
//                    Text(modifier=modifier,text=label)
//                }
//            ),

            dividerProperties = DividerProperties(
                xAxisProperties = LineProperties(enabled = false),  // elimino la linea de origen en X
                yAxisProperties = LineProperties(enabled = false)   // elimino la linea de origen en Y
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed()
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed()
                )
            ),

            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = CustomTextStyles.textStyle12Normal
            ),
            animationDelay = 0,
            animationMode = AnimationMode.Together { 0 },
//            animationMode = AnimationMode.Together(delayBuilder = {
//                it * 500L
//            }),
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
    val dummyDynamicData = remember { mutableStateOf<RobotDynamicData?>(null) }
    val dummyLimitAxis = remember { mutableFloatStateOf(100F) }

//    LineChartCompose(
//        modifier = Modifier,
////        dummyLineData,
////        newPointData = dummyPointList,
//        newData = dummyDynamicData,
//        false,
//        dummyLimitAxis
//    ) {}
}