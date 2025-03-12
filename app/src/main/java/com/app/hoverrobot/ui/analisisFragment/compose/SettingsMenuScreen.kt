package com.app.hoverrobot.ui.analisisFragment.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.analisisFragment.resources.SelectedDataset

@Composable
fun SettingsMenuScreen(
    onActionSettingsMenu: (SettingsMenuActions) -> Unit
) {
    var datasetSelected by remember { mutableIntStateOf(0) }
    var onPauseState by remember { mutableStateOf(false) }
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
            .fillMaxSize()
            .border(border = BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(8.dp))
    ) {
        LazyColumn {
            item {
                ButtonItem(
                    title = if (onPauseState) R.string.btn_play_title else R.string.btn_pause_title
                ) {
                    onPauseState = !onPauseState
                    onActionSettingsMenu(SettingsMenuActions.OnPauseChange(onPauseState))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.dataset_title),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
                        onActionSettingsMenu(SettingsMenuActions.OnDatasetChange(selected))
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
                        onActionSettingsMenu(SettingsMenuActions.OnDatasetChange(null))
                    }
                )
            }

            item {
                SwitchItem(
                    checkedState = autoscaleState,
                    onCheckedChange = {
                        autoscaleState = it
                        onActionSettingsMenu(SettingsMenuActions.OnAutoScaleChange(it))
                    }
                )
            }

            item {
                ButtonItem(
                    title = R.string.btn_clear_title
                ) {
                    onActionSettingsMenu(SettingsMenuActions.OnClearData)
                }
            }
        }
    }
}

@Composable
private fun ButtonItem(
    @StringRes title: Int,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(title)
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
private fun SettingsMenuScreen() {

    Column(Modifier.width(140.dp)) {
        SettingsMenuScreen {}
    }
}
