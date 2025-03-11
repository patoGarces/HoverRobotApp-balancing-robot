package com.example.hoverrobot.ui.settingsFragment.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.PidParams
import com.example.hoverrobot.data.models.comms.RobotLocalConfig

@Composable
fun SettingsFragmentScreen(
    originalRobotConfig: RobotLocalConfig,
    onActionScreen: (OnActionSettingsScreen) -> Unit
) {
    val robotLocalConfig = remember { mutableStateOf(originalRobotConfig) }

    LaunchedEffect(robotLocalConfig.value) {
        onActionScreen(OnActionSettingsScreen.OnNewSettings(robotLocalConfig.value))
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PidSettingsCard(robotLocalConfig)

        GeneralSettingsCard(
            onCalibrateImu = { onActionScreen(OnActionSettingsScreen.OnCalibrateImu) },
            onCleanLeftMotor = { onActionScreen(OnActionSettingsScreen.OnCleanLeftMotor) },
            onCleanRightMotor = { onActionScreen(OnActionSettingsScreen.OnCleanRightMotor) }
        )
    }
}

@Composable
private fun PidSettingsCard(
    robotLocalConfig: MutableState<RobotLocalConfig>
) {
    PidSettingsCardHeader(
        onPidSave = {},
        onPidSync = {},
        onPidReset = {}
    )

    Column(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {

        LazyColumn {
            items(robotLocalConfig.value.pids.size) { index ->
                with(robotLocalConfig.value.pids[index]) {
                    SliderParam(R.string.pid_parameter_p_title, kp) { newValue ->
                        robotLocalConfig.updatePidValue(index) { it.copy(kp = newValue) }
                    }
                    SliderParam(R.string.pid_parameter_i_title, ki) { newValue ->
                        robotLocalConfig.updatePidValue(index) { it.copy(ki = newValue) }
                    }
                    SliderParam(R.string.pid_parameter_d_title, kd) { newValue ->
                        robotLocalConfig.updatePidValue(index) { it.copy(kd = newValue) }
                    }
                }
            }

            item { // TODO: es distinto este slider, tiene el centro en 0, con el wording "front", "back"
                SliderParam(R.string.pid_parameter_center_title, robotLocalConfig.value.centerAngle) {}
            }

            item {
                SliderParam(R.string.pid_parameter_limits_title, robotLocalConfig.value.centerAngle) {}
            }
        }
    }
}

// TODO: falta controlar visibilidad de botones, enables, etc
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun PidSettingsCardHeader(
    onPidReset: () -> Unit,
    onPidSave: () -> Unit,
    onPidSync: () -> Unit
) {
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }
    var dropDownMenuSelectedItem by remember { mutableIntStateOf(0) }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleSectionText(R.string.pid_group_title)

        Spacer(Modifier.weight(1F))

        ButtonSection(R.string.btn_pid_reset, onClick = onPidReset)

        ButtonSection(R.string.btn_pid_save, onClick = onPidSave)

        ButtonSection(R.string.btn_pid_sync, onClick = onPidSync)

        // TODO: sacar esto de aca:
        val optionDropDownMenu = listOf("PID ANGLE","PID POS","PID SPEED","PID YAW")

        ExposedDropdownMenuBox(
            expanded = isDropdownMenuExpanded,
            onExpandedChange = { isDropdownMenuExpanded = it },
        ) {
            Row(
                Modifier
                    .width(130.dp)
                    .height(35.dp)
                    .padding(horizontal = 8.dp)
                    .border(width = 1.dp, color = Color.Red, shape = RoundedCornerShape(8.dp))
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1F)
                        .padding(start = 8.dp),
                    state = TextFieldState(initialText = optionDropDownMenu.getOrNull(dropDownMenuSelectedItem) ?: ""),
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )

                val trailingIcon = if (isDropdownMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
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
                onDismissRequest = { isDropdownMenuExpanded = false }) {
                optionDropDownMenu.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item, color = Color.White) },
                        onClick = {
                            dropDownMenuSelectedItem = index
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
    onCalibrateImu: () -> Unit,
    onCleanLeftMotor: () -> Unit,
    onCleanRightMotor: () -> Unit
) {
    TitleSectionText(R.string.pid_group_title)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(8.dp))
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
    onClickFirst: () -> Unit,
    onClickSecond: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(titleItem),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.weight(1F))

        ButtonSection(firstButtonTitle, onClick = onClickFirst)

        secondButtonTitle?.let { title ->
            ButtonSection(title, onClick = onClickSecond ?: {})
        }
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 32.dp),
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
        color = Color.White
    )
}

@Composable
private fun ButtonSection(
    @StringRes title: Int,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    Column (
        modifier = Modifier
            .widthIn(min = 100.dp).height(50.dp)
            .padding(8.dp)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.Red
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enable,
            modifier = Modifier.fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(title),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderParam(
    @StringRes nameId: Int,
    actualValue: Float,
    range: ClosedFloatingPointRange<Float> = 0F..1F,
    onValueChange: (Float) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(nameId),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var sliderValue by remember { mutableFloatStateOf(0f) }

            Box(Modifier.weight(1F)) {

                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                        .background(
                            Color.Gray,
                            RoundedCornerShape(2.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .fillMaxWidth(fraction = sliderValue)
                        .align(Alignment.CenterStart)
                        .background(
                            Color.Red,
                            RoundedCornerShape(2.dp)
                        )
                )

                Slider(
                    value = sliderValue,
                    onValueChange = onValueChange,
                    valueRange = range,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Red,
                        inactiveTrackColor = Color.Transparent,
                        activeTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            Modifier
                                .size(18.dp)
                                .align(Alignment.Center)
                                .background(Color.Red, CircleShape)
                        )
                    }
                )
            }

            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.value_slider_format, actualValue),
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

// TODO: sacar de aca
private fun MutableState<RobotLocalConfig>.updatePidValue(index: Int, updateField: (PidParams) -> PidParams) {
    value = value.copy(
        pids = value.pids.toMutableList().apply {
            this[index] = updateField(this[index])
        }
    )
}

@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
private fun SettingsFragmentScreenPreview() {

    val localConfig = RobotLocalConfig(
        pids = listOf(
            PidParams(1f, 2f, 3f)
        ),
        centerAngle = 4f,
        safetyLimits = 5f
    )
    SettingsFragmentScreen(localConfig) {}

}
