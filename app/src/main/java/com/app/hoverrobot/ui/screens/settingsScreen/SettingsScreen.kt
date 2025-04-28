package com.app.hoverrobot.ui.screens.settingsScreen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.models.comms.PidParams
import com.app.hoverrobot.data.models.comms.PidSettings
import com.app.hoverrobot.data.models.comms.RobotLocalConfig
import com.app.hoverrobot.data.models.comms.asPidSettings
import com.app.hoverrobot.data.models.comms.isDiffWithOriginalLocalConfig
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomDropdownMenu
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomSlider
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun SettingsScreen(
    localRobotConfig: RobotLocalConfig,
    statusRobot: StatusRobot,
    networkState: NetworkState,
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

        ConnectionSettingsCard(
            networkState = networkState,
            onReconnectRobot =  { onActionScreen(SettingsScreenActions.OnReconnectToRobot(it)) },
            onReconnectRaspi =  { onActionScreen(SettingsScreenActions.OnReconnectToRaspi(it)) }
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
    var buttonSaveEnable by remember { mutableStateOf(true) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleSectionText(R.string.settings_group_pid_title)

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

        CustomDropdownMenu(R.array.dropdown_menu_pid_items, onIndexChange = onIndexPidChange)
    }
}

@Composable
private fun GeneralSettingsCard(
    statusRobot: StatusRobot,
    onCalibrateImu: () -> Unit,
    onCleanLeftMotor: () -> Unit,
    onCleanRightMotor: () -> Unit
) {
    TitleSectionText(R.string.settings_group_general_title)

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
private fun ConnectionSettingsCard(
    networkState: NetworkState,
    onReconnectRobot: (String) -> Unit,
    onReconnectRaspi: (String) -> Unit,
) {
    TitleSectionText(R.string.settings_group_connection_title)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(8.dp))
    ) {
        networkState.statusRobotClient.addressIp?.let { clientRobotAddress ->
            IpAddressItem(
                titleItem = R.string.settings_connection_robot_title,
                ipAddress = clientRobotAddress,
                onConfirmClick = onReconnectRobot
            )
        }

        networkState.statusRaspiClient.addressIp?.let { clientRaspiAddress ->
            IpAddressItem(
                titleItem = R.string.settings_connection_raspi_title,
                ipAddress = clientRaspiAddress,
                onConfirmClick = onReconnectRaspi
            )
        }
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
            color = MaterialTheme.colorScheme.onBackground,
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
private fun IpAddressItem(
    @StringRes titleItem: Int,
    ipAddress: String,
    onConfirmClick: (String) -> Unit
){

    var textFieldValue by remember { mutableStateOf(TextFieldValue(ipAddress.substringAfterLast("."))) }
    var isError by remember { mutableStateOf(false) }
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
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.weight(1F))

        Text(
            text = ipAddress.substringBeforeLast(".") + ".",
            style = CustomTextStyles.textStyle14Bold,
        )

        Box(
            modifier = Modifier
                .heightIn(min = 35.dp)
                .width(60.dp)
                .border(
                    border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.text.filter { it.isDigit() }.take(3)

                    val normalizedText = when {
                        digitsOnly.isEmpty() -> ""
                        digitsOnly.all { it == '0' } -> "0"     // Si son solo ceros, muestro "0"
                        else -> digitsOnly.trimStart('0')       // Si no, limpio los ceros de adelante
                    }

                    textFieldValue = TextFieldValue(
                        text = normalizedText,
                        selection = TextRange(digitsOnly.length) // Siempre poner cursor al final
                    )
                    isError = when {
                        digitsOnly.isEmpty() -> true
                        digitsOnly.toIntOrNull()?.let { it > 255 } == true -> true
                        else -> false
                    }
                },
                singleLine = true,
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = CustomTextStyles.textStyle14Bold,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
            )
        }

        Spacer(Modifier.width(16.dp))

        val colorButton = if (isError) Color.Gray else MaterialTheme.colorScheme.onBackground
        Box(
            modifier = Modifier
                .size(35.dp)
                .border(
                    BorderStroke(1.dp, color = colorButton),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onConfirmClick("192.168.0.${textFieldValue.text}") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                tint = colorButton,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TitleSectionText(@StringRes nameId: Int) {

    Text(
        modifier = Modifier.padding(vertical = 16.dp),
        text = stringResource(nameId),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
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
            color = MaterialTheme.colorScheme.onBackground,
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
                color = MaterialTheme.colorScheme.onBackground,
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Text(
                    text = edgeIndicators.second,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.padding(8.dp))
        }
    }
}

@Composable
@CustomPreview
private fun SettingsScreenPreview() {

    val localConfig = RobotLocalConfig(
        pids = listOf(
            PidParams(1f, 2f, 3f)
        ),
        centerAngle = 4f,
        safetyLimits = 5f
    )

    val networkStateMock = NetworkState(
        statusRobotClient = ConnectionState(
            status = StatusConnection.CONNECTED,
            addressIp = "255.255.255.254"
        ),
        statusRaspiClient = ConnectionState(
            status = StatusConnection.WAITING,
            addressIp = "255.255.255.255"
        ),
        localIp = "192.168.0.0"
    )

    MyAppTheme {
        SettingsScreen(
            localRobotConfig = localConfig,
            statusRobot = StatusRobot.STABILIZED,
            networkState = networkStateMock,
            onPidSave = { false }
        ) {}
    }
}
