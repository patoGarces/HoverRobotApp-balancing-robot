package com.app.hoverrobot.ui.screens.settingsScreen

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.PidParams
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.comms.asPidSettings
import com.app.hoverrobot.data.models.comms.isDiffWithOriginalLocalConfig
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomSlider
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun SettingsScreen(
    localRobotConfig: RobotLocalConfig,
    statusRobot: StatusRobot,
    onPidSave: (PidSettings) -> Boolean,
    onActionScreen: (SettingsScreenActions) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PidSettingsCard(
            originalLocalConfig = localRobotConfig,
            onSendPidSettings = { onActionScreen(SettingsScreenActions.OnNewSettings(it)) },
            onPidSave = { onPidSave(it) },
        )

        GeneralSettingsCard(
            statusRobot = statusRobot,
            onCalibrateImu = { onActionScreen(SettingsScreenActions.OnCalibrateImu) },
            onCleanLeftMotor = { onActionScreen(SettingsScreenActions.OnCleanLeftMotor) },
            onCleanRightMotor = { onActionScreen(SettingsScreenActions.OnCleanRightMotor) }
        )
    }
}

@Composable
private fun PidSettingsCard(
    originalLocalConfig: RobotLocalConfig,
    onSendPidSettings: (PidSettings) -> Unit,
    onPidSave: (PidSettings) -> Boolean,
) {
    var indexPid by remember { mutableIntStateOf(0) }
    var newPidSettings by remember { mutableStateOf(originalLocalConfig.asPidSettings(indexPid)) }
    var enablePidSave by remember { mutableStateOf(false) }
    var enablePidReset by remember { mutableStateOf(false) }

    // TODO: simplificar estos 2 launchedEffect en 1 solo
    LaunchedEffect(originalLocalConfig) {
        newPidSettings = originalLocalConfig.asPidSettings(indexPid)
        enablePidReset = newPidSettings.isDiffWithOriginalLocalConfig(originalLocalConfig)
        enablePidSave = newPidSettings.isDiffWithOriginalLocalConfig(originalLocalConfig)
    }

    LaunchedEffect(newPidSettings) {
        enablePidReset = newPidSettings.isDiffWithOriginalLocalConfig(originalLocalConfig)
        enablePidSave = newPidSettings.isDiffWithOriginalLocalConfig(originalLocalConfig)
        onSendPidSettings(newPidSettings)
    }

    PidSettingsCardHeader(
        enablePidSave = enablePidSave,
        onPidSave = { onPidSave(newPidSettings) },
        onPidSync = { onSendPidSettings(newPidSettings) }, // simplemente reenvio el pidSetting
        enablePidReset = enablePidReset,
        onPidReset = {
            newPidSettings = originalLocalConfig.asPidSettings(indexPid)
            onSendPidSettings(newPidSettings)
        },
        onIndexPidChange = {
            indexPid = it
            newPidSettings = originalLocalConfig.asPidSettings(indexPid)
        }
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            SliderParam(
                nameId = R.string.pid_parameter_p_title,
                initialValue = newPidSettings.kp
            ) { newValue ->
                newPidSettings = newPidSettings.copy(kp = newValue)
            }
            SliderParam(
                nameId = R.string.pid_parameter_i_title,
                initialValue = newPidSettings.ki
            ) { newValue ->
                newPidSettings = newPidSettings.copy(ki = newValue)
            }
            SliderParam(
                nameId = R.string.pid_parameter_d_title,
                initialValue = newPidSettings.kd
            ) { newValue ->
                newPidSettings = newPidSettings.copy(kd = newValue)
            }

            SliderParam(
                nameId = R.string.pid_parameter_center_title,
                edgeIndicators = Pair(
                    stringResource(R.string.pid_center_back),
                    stringResource(R.string.pid_center_front)
                ),
                initialValue = newPidSettings.centerAngle,
                range = -10F..10F
            ) { newCenterAngle ->
                newPidSettings = newPidSettings.copy(centerAngle = newCenterAngle)
            }

            SliderParam(
                nameId = R.string.pid_parameter_limits_title,
                stepSize = 1F,
                initialValue = newPidSettings.safetyLimits,
                range = 0F..60F                                             // TODO: traer de afuera
            ) { newSafetyLimits ->
                newPidSettings = newPidSettings.copy(safetyLimits = newSafetyLimits)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun PidSettingsCardHeader(
    enablePidReset: Boolean,
    onPidReset: () -> Unit,
    enablePidSave: Boolean,
    onPidSave: () -> Boolean,
    onPidSync: () -> Unit,
    onIndexPidChange: (Int) -> Unit
) {
    val listColor = listOf(
        Color.White,
        Color.Blue,
        Color.Green,
        Color.Yellow
    )
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }
    var dropDownMenuSelectedItem by remember { mutableIntStateOf(0) }
    var buttonSaveEnable by remember { mutableStateOf(true) }
    val optionDropDownMenu = stringArrayResource(R.array.dropdown_menu_pid_items)
    val outlineColor by remember { derivedStateOf { listColor[dropDownMenuSelectedItem] } }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleSectionText(R.string.pid_group_title)

        Spacer(Modifier.weight(1F))

        CustomButton(
            title = stringResource(R.string.btn_pid_reset),
            modifier = Modifier.padding(horizontal = 8.dp),
            enable = enablePidReset
        ) { onPidReset() }

        CustomButton(
            title = stringResource(R.string.btn_pid_save),
            modifier = Modifier.padding(horizontal = 8.dp),
            enable = enablePidSave
        ) { buttonSaveEnable = onPidSave() }

        CustomButton(
            title = stringResource(R.string.btn_pid_sync),
            modifier = Modifier.padding(horizontal = 8.dp),
        ) { onPidSync() }

        ExposedDropdownMenuBox(
            expanded = isDropdownMenuExpanded,
            onExpandedChange = { },
        ) {
            Row(
                Modifier
                    .width(130.dp)
                    .height(35.dp)
                    .padding(horizontal = 8.dp)
                    .border(width = 1.dp, color = outlineColor, shape = RoundedCornerShape(8.dp))
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .clickable { isDropdownMenuExpanded = !isDropdownMenuExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = optionDropDownMenu.getOrNull(dropDownMenuSelectedItem) ?: "Unknown",
                    style = CustomTextStyles.textStyle14Bold
                )

                val trailingIcon =
                    if (isDropdownMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "Dropdown",
                    tint = Color.White,
                )
            }

            ExposedDropdownMenu(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                    .background(Color.Black, RoundedCornerShape(8.dp)),
                containerColor = Color.Transparent,
                expanded = isDropdownMenuExpanded,
                onDismissRequest = { }
            ) {
                optionDropDownMenu.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item, color = Color.White) },
                        onClick = {
                            dropDownMenuSelectedItem = index
                            onIndexPidChange(index)
                            isDropdownMenuExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralSettingsCard(
    statusRobot: StatusRobot,
    onCalibrateImu: () -> Unit,
    onCleanLeftMotor: () -> Unit,
    onCleanRightMotor: () -> Unit
) {
    TitleSectionText(R.string.pid_group_title)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(8.dp))
    ) {
        GeneralSettingsItem(
            titleItem = R.string.general_commands_calibrate_imu_title,
            firstButtonTitle = R.string.general_commands_calibrate_imu_title,
            onClickFirst = onCalibrateImu
        )

        GeneralSettingsItem(
            titleItem = R.string.general_command_clean_wheels_title,
            firstButtonTitle = R.string.general_command_btn_clean_left,
            secondButtonTitle = R.string.general_command_btn_clean_right,
            isLoading = statusRobot == StatusRobot.TEST_MODE,
            onClickFirst = onCleanLeftMotor,
            onClickSecond = onCleanRightMotor
        )
    }
}

@Composable
private fun GeneralSettingsItem(
    @StringRes titleItem: Int,
    @StringRes firstButtonTitle: Int,
    @StringRes secondButtonTitle: Int? = null,
    isLoading: Boolean = false,
    onClickFirst: () -> Unit,
    onClickSecond: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(titleItem),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        Spacer(Modifier.weight(1F))

        CustomButton(
            stringResource(firstButtonTitle),
            onClick = onClickFirst,
            isLoading = isLoading
        )

        secondButtonTitle?.let { title ->
            Spacer(Modifier.width(8.dp))

            CustomButton(
                stringResource(title),
                onClick = onClickSecond ?: {},
                isLoading = isLoading
            )
        }
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color.Gray
    )
}

@Composable
private fun TitleSectionText(@StringRes nameId: Int) {

    Text(
        modifier = Modifier.padding(vertical = 16.dp),
        text = stringResource(nameId),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
    )
}

@Composable
private fun SliderParam(
    @StringRes nameId: Int,
    edgeIndicators: Pair<String, String>? = null,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float> = 0F..4F,
    stepSize: Float = 0.01F,
    onValueChange: (Float) -> Unit
) {
    var actualValue by remember { mutableFloatStateOf(initialValue) }

    LaunchedEffect(initialValue) {
        actualValue = initialValue
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(nameId),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomSlider(
                modifier = Modifier.weight(1F),
                initialValue = actualValue,
                range = range,
                stepSize = stepSize,
                onUpdateValue = { actualValue = it }
            ) {
                actualValue = it
                onValueChange(it)
            }

            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.value_slider_format, actualValue),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        edgeIndicators?.let {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = edgeIndicators.first,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
                Text(
                    text = edgeIndicators.second,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.padding(8.dp))
        }
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
private fun SettingsScreenPreview() {

    val localConfig = RobotLocalConfig(
        pids = listOf(
            PidParams(1f, 2f, 3f)
        ),
        centerAngle = 4f,
        safetyLimits = 5f
    )

    MyAppTheme {
        SettingsScreen(
            localRobotConfig = localConfig,
            statusRobot = StatusRobot.STABILIZED,
            onPidSave = { false }
        ) {}
    }
}
