package com.app.hoverrobot.ui.analisisFragment.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.analisisFragment.resources.SelectedDataset
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomColors
import com.app.hoverrobot.ui.composeUtils.LineChartCompose
import com.github.mikephil.charting.data.LineData

@Composable
fun AnalisisScreen(
    dynamicData: State<RobotDynamicData?>,
    actualLineData: State<LineData?>,
    limitAxis: State<Float>,
    statusRobot: State<StatusRobot?>,
    onActionAnalisisScreen: (AnalisisScreenActions) -> Unit
) {
    var logMode by remember { mutableStateOf(false) }
    var onPauseState by remember { mutableStateOf(false) }
    var isAutoScaled by remember { mutableStateOf(false) }

    val listOfLogs = remember { mutableStateListOf<Triple<Long, StatusRobot, String?>>() }

    LaunchedEffect(statusRobot.value) {
        statusRobot.value?.let {
            if (!listOfLogs.isNotEmpty() || listOfLogs.last().second != statusRobot.value) {
                listOfLogs.add(0, Triple(System.currentTimeMillis(), it, null))
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1F)) {
            if (logMode) {
                LogScreen(listOfLogs = listOfLogs)
            } else {
                Box {
                    LineChartCompose(
                        actualLineData,
                        isAutoScaled = isAutoScaled,
                        limitAxes = limitAxis
                    )

                    CustomFloatingButton(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        icon = if (onPauseState) Icons.Filled.PlayArrow else Icons.Filled.Pause
                    ) {
                        onPauseState = !onPauseState
                        onActionAnalisisScreen(AnalisisScreenActions.OnPauseChange(onPauseState))
                    }
                }
            }

            dynamicData.value?.let {
                HighlightValues(dynamicData)
            }
        }

        SettingsChartMenuScreen(
            onClearChart = { onActionAnalisisScreen(AnalisisScreenActions.OnClearData) },
            onDatasetChange = {
                logMode = it == null
                onActionAnalisisScreen(AnalisisScreenActions.OnDatasetChange(it))
            },
            onAutoScaleChange = { isAutoScaled = it },
        )
    }
}

@Composable
private fun HighlightValues(dynamicItem: State<RobotDynamicData?>) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = stringResource(R.string.placeholder_pitch, dynamicItem.value!!.pitchAngle),
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_roll, dynamicItem.value!!.rollAngle),
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_yaw, dynamicItem.value!!.yawAngle),
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_center, dynamicItem.value!!.centerAngle),
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_position, dynamicItem.value!!.posInMeters),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SettingsChartMenuScreen(
    onClearChart: () -> Unit,
    onDatasetChange: (SelectedDataset?) -> Unit,
    onAutoScaleChange: (Boolean) -> Unit
) {
    var datasetSelected by remember { mutableIntStateOf(0) }
    var autoscaleState by remember { mutableStateOf(false) }

    val mapTitleDataset = listOf(
        R.string.dataset_imu_data,
        R.string.dataset_power_data,
        R.string.dataset_pid_angle,
        R.string.dataset_pid_pos,
        R.string.dataset_pid_yaw,
        R.string.dataset_pid_speed,
    )

    Column(
        modifier = Modifier
            .width(140.dp)
            .border(border = BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(8.dp))
    ) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.dataset_title),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            }

            items(SelectedDataset.entries.size) { item ->
                CheckboxItem(
                    nameItem = mapTitleDataset[item],
                    indexItem = item,
                    datasetSelected = datasetSelected,
                    onItemSelected = {
                        datasetSelected = item
                        val selected = SelectedDataset.entries.find { it.ordinal == item }!!
                        onDatasetChange(selected)
                    }
                )
            }

            item {
                CheckboxItem(
                    nameItem = R.string.dataset_log_mode,
                    indexItem = SelectedDataset.entries.size,
                    datasetSelected = datasetSelected,
                    onItemSelected = {
                        datasetSelected = SelectedDataset.entries.size
                        onDatasetChange(null)
                    }
                )
            }

            item {
                SwitchItem(
                    checkedState = autoscaleState,
                    onCheckedChange = {
                        autoscaleState = it
                        onAutoScaleChange(it)
                    }
                )
            }

            item {
                CustomButton(
                    title = stringResource(R.string.btn_clear_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = CustomColors.PurpleThemeDefault,
                ) {
                    onClearChart()
                }
            }
        }
    }
}

@Composable
private fun CustomFloatingButton(
    modifier: Modifier,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 2.dp,
                color = CustomColors.PurpleThemeDefault,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            tint = Color.White,
            contentDescription = "Clear logs"
        )
    }
}

@Composable
private fun CheckboxItem(
    @StringRes nameItem: Int,
    indexItem: Int,
    datasetSelected: Int?,
    onItemSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemSelected() }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(
            checked = indexItem == datasetSelected,
            onCheckedChange = { if (it) onItemSelected() },
        )

        Text(
            text = stringResource(nameItem),
            style = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SwitchItem(
    checkedState: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checkedState) }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Switch(
            checked = checkedState,
            onCheckedChange = { onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = Color.Transparent
            )
        )

        Text(
            text = stringResource(R.string.switch_text_title),
            style = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun AnalisisScreenPreview() {
    val dummyDynamicData = remember { mutableStateOf<RobotDynamicData?>(null) }
    val dummyLineData = remember { mutableStateOf<LineData?>(null) }
    val dummyStatusRobot = remember { mutableStateOf(StatusRobot.TEST_MODE) }
    val dummyLimitAxis = remember { mutableFloatStateOf(100F) }

    Column {
        AnalisisScreen(
            actualLineData = dummyLineData,
            dynamicData = dummyDynamicData,
            limitAxis = dummyLimitAxis,
            statusRobot = dummyStatusRobot
        ) {}
    }
}
