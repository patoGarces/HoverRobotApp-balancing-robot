package com.app.hoverrobot.ui.screens.analisisScreen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.ChartLimitsConfig
import com.app.hoverrobot.data.models.comms.FrameRobotDynamicData
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.Temperatures
import com.app.hoverrobot.data.models.comms.CollisionSensors
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnClearData
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnDatasetChange
import com.app.hoverrobot.ui.screens.analisisScreen.AnalisisScreenActions.OnPauseChange
import com.app.hoverrobot.ui.screens.analisisScreen.compose.LogScreen
import com.app.hoverrobot.ui.screens.analisisScreen.resources.SelectedDataset
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.LineChartCompose
import com.app.hoverrobot.ui.theme.MyAppTheme
import com.github.mikephil.charting.data.LineData

@Composable
fun AnalisisScreen(
    lastDynamicData: State<FrameRobotDynamicData?>,
    actualLineData: State<LineData?>,
    chartLimitsConfig: State<ChartLimitsConfig>,
    historicStatusRobot: List<Triple<Long, StatusRobot, String?>>,
    isPaused: Boolean,
    onActionAnalisisScreen: (AnalisisScreenActions) -> Unit
) {
    var isAutoScaled by rememberSaveable { mutableStateOf(false) }
    var indexDataset by rememberSaveable { mutableIntStateOf(0) }

    Row(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (indexDataset == SelectedDataset.entries.size) {
            LogScreen(
                modifier = Modifier.weight(1F),
                listOfLogs = historicStatusRobot
            ) { onActionAnalisisScreen(AnalisisScreenActions.OnClearLogs)}
        } else {
            Column(Modifier.weight(1F)) {
                LineChartCompose(
                    modifier = Modifier.weight(1F),
                    actualLineData = actualLineData,
                    isAutoScaled = isAutoScaled,
                    isPaused = isPaused,
                    chartLimitsConfig = chartLimitsConfig,
                ) {
                    onActionAnalisisScreen(OnPauseChange(it))
                }

                lastDynamicData.value?.robotData?.let { HighlightValues(it) }
            }
        }

        Spacer(Modifier.width(8.dp))

        SettingsChartMenuScreen(
            initAutoScale = isAutoScaled,
            selectedDataset = indexDataset,
            onClearChart = { onActionAnalisisScreen(OnClearData) },
            onIndexDatasetChange = {
                indexDataset = it
                if (it < SelectedDataset.entries.size) {
                    val dataset = SelectedDataset.entries.find { it.ordinal == indexDataset }!!
                    onActionAnalisisScreen(OnDatasetChange(dataset))
                }
            },
            onAutoScaleChange = { isAutoScaled = it },
        )
    }
}

@Composable
private fun HighlightValues(dynamicItem: RobotDynamicData) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = stringResource(R.string.placeholder_pitch, dynamicItem.pitchAngle),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_roll, dynamicItem.rollAngle),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_yaw, dynamicItem.yawAngle),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )

        Text(
            text = stringResource(R.string.placeholder_position, dynamicItem.posInMeters),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SettingsChartMenuScreen(
    initAutoScale: Boolean,
    selectedDataset: Int,
    onClearChart: () -> Unit,
    onIndexDatasetChange: (Int) -> Unit,
    onAutoScaleChange: (Boolean) -> Unit
) {
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
            .border(border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), shape = RoundedCornerShape(8.dp))
    ) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.dataset_title),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            items(SelectedDataset.entries.size) { item ->
                CheckboxItem(
                    nameItem = mapTitleDataset[item],
                    indexItem = item,
                    datasetSelected = selectedDataset,
                    onItemSelected = { onIndexDatasetChange(item) }
                )
            }

            item {
                CheckboxItem(
                    nameItem = R.string.dataset_log_mode,
                    indexItem = SelectedDataset.entries.size,
                    datasetSelected = selectedDataset,
                    onItemSelected = { onIndexDatasetChange(SelectedDataset.entries.size) }
                )
            }

            item {
                SwitchItem(
                    checkedState = initAutoScale,
                    onCheckedChange = {
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
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    onClearChart()
                }
            }
        }
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
                color = MaterialTheme.colorScheme.onBackground,
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
                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = Color.Transparent
            )
        )

        Text(
            text = stringResource(R.string.switch_text_title),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@CustomPreview
@Composable
private fun AnalisisScreenPreview() {
    val mockRobotDynamicData = RobotDynamicData(
        isCharging = false,
        batVoltage = 39.2F,
        temperatures = Temperatures(
            tempImu = 36.5F,
            tempMcb = 40.1F,
            tempMainboard = 42.3F,
        ),
        speedR = 10F,
        speedL = 10F,
        poswheelR = 10F,
        posWheelL = 10F,
        currentR = 1.2F,
        currentL = 1.3F,
        pitchAngle = 0.5F,
        rollAngle = -1.2F,
        yawAngle = 3.4F,
        collisionSensors = CollisionSensors(),
        posInMeters = 12.7F,
        outputYawControl = 0.8F,
        setPointAngle = 0.0F,
        setPointPos = 15.0F,
        setPointYaw = 2.1F,
        setPointSpeed = 5.0F,
        statusCode = StatusRobot.STABILIZED
    )

    val dummyFrameDynamicData = remember { mutableStateOf<FrameRobotDynamicData?>(
        FrameRobotDynamicData(mockRobotDynamicData, 0F))
    }
    val dummyLineData = remember { mutableStateOf<LineData?>(null) }
    val dummyStatusRobot = mutableListOf(Triple(0L, StatusRobot.STABILIZED, null))
    val dummyChartLimitsConfig = remember { mutableStateOf(ChartLimitsConfig(100F, null)) }

    MyAppTheme {
        Column {
            AnalisisScreen(
                lastDynamicData = dummyFrameDynamicData,
                actualLineData = dummyLineData,
                chartLimitsConfig = dummyChartLimitsConfig,
                isPaused = false,
                historicStatusRobot = dummyStatusRobot
            ) {}
        }
    }
}
