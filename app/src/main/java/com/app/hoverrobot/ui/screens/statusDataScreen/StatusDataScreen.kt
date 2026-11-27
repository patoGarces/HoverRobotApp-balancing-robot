package com.app.hoverrobot.ui.screens.statusDataScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.comms.CollisionSensors
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.CpuData
import com.app.hoverrobot.data.models.comms.DiskData
import com.app.hoverrobot.data.models.comms.MemoryData
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.models.comms.RobotDynamicData
import com.app.hoverrobot.data.models.comms.TelemetryData
import com.app.hoverrobot.data.models.comms.Temperatures
import com.app.hoverrobot.data.models.toPercentLevel
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Bold
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Normal
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun StatusDataScreen(
    telemetryState: TelemetryData?,
    networkState: NetworkState,
    robotDynamicData: RobotDynamicData?,
    onOpenNetworkSettings: () -> Unit,
    onShutdownAPS: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TitleScreen(stringResource(R.string.title_status_screen)) {
            onOpenNetworkSettings()
        }

        VehicleAndControlSystemSection(
            robotDynamicData = robotDynamicData,
            connectionState = networkState.statusRobotClient
        )

        AutonomyPerceptionSection(
            telemetryData = telemetryState,
            connectionState = networkState.statusRaspiClient,
            onShutdown = onShutdownAPS
        )

        Version()
    }
}

@Composable
private fun TitleScreen(text: String, onOpenNetworkSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp).weight(1f),
            text = text,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(30.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onOpenNetworkSettings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun VehicleAndControlSystemSection(
    robotDynamicData: RobotDynamicData?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_status_robot),
                    style = CustomTextStyles.textStyle20Bold,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.aps_robot_model_title),
                    style = textStyle14Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = connectionState.status.toColor(),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = stringResource(connectionState.status.toStringRes()),
                        style = textStyle14Bold,
                        fontWeight = FontWeight.SemiBold,
                        color = connectionState.status.toColor()
                    )
                }

                Text(
                    text = connectionState.addressIp
                        ?: stringResource(R.string.unknown_ip),
                    style = textStyle14Normal,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            RobotBatteryMetric(
                robotDynamicData?.batVoltage,
                robotDynamicData?.isCharging ?: false,
                Modifier.weight(1f)
            )

            RobotTemperatureMetric(
                temperatures = robotDynamicData?.temperatures,
                modifier = Modifier.weight(1f)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun AutonomyPerceptionSection(
    telemetryData: TelemetryData?,
    connectionState: ConnectionState,
    onShutdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showShutdownConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_status_raspi),
                    style = CustomTextStyles.textStyle20Bold,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.aps_raspi_model_title),
                    style = textStyle14Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = connectionState.status.toColor(),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = stringResource(connectionState.status.toStringRes()),
                        style = textStyle14Bold,
                        fontWeight = FontWeight.SemiBold,
                        color = connectionState.status.toColor()
                    )
                }

                Text(
                    text = connectionState.addressIp
                        ?: stringResource(R.string.unknown_ip),
                    style = textStyle14Normal,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CpuMetric(
                cpu = telemetryData?.cpu.takeIf { connectionState.status == StatusConnection.CONNECTED },
                modifier = Modifier.weight(1f)
            )

            MemoryMetric(
                memory = telemetryData?.memory.takeIf { connectionState.status == StatusConnection.CONNECTED },
                modifier = Modifier.weight(1f)
            )

            FanMetric(
                fans = telemetryData?.fans.takeIf { connectionState.status == StatusConnection.CONNECTED },
                modifier = Modifier.weight(1f)
            )

            UptimeMetric(
                uptime = telemetryData?.uptime.takeIf { connectionState.status == StatusConnection.CONNECTED } ?: 0,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        TextButton(
            onClick = { showShutdownConfirmation = true },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Apagar APS")
        }
    }

    if (showShutdownConfirmation) {
        AlertDialog(
            onDismissRequest = { showShutdownConfirmation = false },
            title = {
                Text("Apagar Raspberry Pi")
            },
            text = {
                Text("¿Estás seguro de que querés apagar el sistema APS?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showShutdownConfirmation = false
                        onShutdown()
                    }
                ) {
                    Text(
                        "Apagar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showShutdownConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RobotBatteryMetric(
    batVoltage: Float?,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val percentage = batVoltage?.toPercentLevel() ?: 0
    val percentageColor = when {
        percentage <= 20 -> Color(0xFFE57373)
        percentage <= 40 -> Color(0xFFFFB74D)
        else -> MaterialTheme.colorScheme.onBackground
    }
    val cellVoltage = batVoltage?.let { it / 10.0f } ?: 0f

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.status_screen_vcs_battery_title),
            style = textStyle14Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Cargando",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(19.dp)
                )
            }

            Text(
                text = "$percentage%",
                style = CustomTextStyles.textStyle24Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.weight(1f))

            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        ) {
                            append("%.2f V".format(batVoltage))
                        }

                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize
                            )
                        ) {
                            append(" Total")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        ) {

                            append("%.2f V".format(cellVoltage))
                        }

                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize
                            )
                        ) {
                            append(" Por celda")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { percentage/100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = percentageColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun RobotTemperatureMetric(
    temperatures: Temperatures?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TemperatureValue(
            title = stringResource(R.string.status_screen_vcs_mainboard_title),
            temperature = temperatures?.tempMainboard ?: 0f
        )

        TemperatureValue(
            title = stringResource(R.string.status_screen_vcs_motorboard_title),
            temperature = temperatures?.tempMcb ?: 0f
        )

        TemperatureValue(
            title = stringResource(R.string.status_screen_vcs_imu_title),
            temperature = temperatures?.tempImu ?: 0f
        )
    }
}

@Composable
private fun TemperatureValue(
    title: String,
    temperature: Float
) {
    val color = when {
        temperature >= 80f -> MaterialTheme.colorScheme.error
        temperature >= 65f -> Color(0xFFE6A23C)
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val text = if (temperature != 0f)
            "%.1f°C".format(temperature)
            else stringResource(R.string.variable_not_available)
        Text(
            text = text,
            style = CustomTextStyles.textStyle24Normal,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun CpuMetric(
    cpu: CpuData?,
    modifier: Modifier = Modifier
) {
    val progress = cpu?.usage?.let { (it / 100f).coerceIn(0f, 1f) } ?: 0f

    val color = when {
        cpu?.usage != null && cpu.usage >= 85f -> MaterialTheme.colorScheme.error
        cpu?.usage != null && cpu.usage >= 65f -> Color(0xFFE6A23C)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.status_screen_aps_cpu_title),
            style = textStyle14Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = cpu?.usage?.let { "%.0f%%".format(it) } ?: stringResource(R.string.variable_not_available),
                style = CustomTextStyles.textStyle24Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = cpu?.temp?.let { "%.1f°C".format(it) } ?: stringResource(R.string.variable_not_available),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun MemoryMetric(
    memory: MemoryData?,
    modifier: Modifier = Modifier
) {
    val progress = memory?.usage?.let { (it / 100f).coerceIn(0f, 1f) } ?: 0f
    val usedMb = if (memory?.totalMb != null)
            (memory.totalMb - memory.availableMb).coerceAtLeast(0)
        else 0

    val usedGb = usedMb / 1024f
    val totalGb = memory?.totalMb?.let { it / 1024f } ?: 0f

    val color = when {
        memory?.usage != null && memory.usage >= 90f -> MaterialTheme.colorScheme.error
        memory?.usage != null && memory.usage >= 75f -> Color(0xFFE6A23C)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.status_screen_aps_ram_title),
            style = textStyle14Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            val usageText = if (memory?.usage != null) "%.0f%%".format(memory.usage)
                else stringResource(R.string.variable_not_available)
            Text(
                text = usageText,
                style = CustomTextStyles.textStyle24Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "%.1f / %.1f GB".format(usedGb, totalGb),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun FanMetric(
    fans: Map<String, Int>?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.status_screen_aps_fans_title),
            style = textStyle14Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (fans.isNullOrEmpty()) {
            Text(
                text = stringResource(R.string.variable_not_available),
                style = CustomTextStyles.textStyle24Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            fans.entries.forEach { (name, rpm) ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "$rpm",
                        style = CustomTextStyles.textStyle24Normal,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "RPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UptimeMetric(
    uptime: Long,
    modifier: Modifier = Modifier
) {
    val hours = (uptime % 86400) / 3600
    val minutes = (uptime % 3600) / 60
    val seconds = uptime % 60

    val uptimeText = when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m ${seconds}s"
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.status_screen_aps_uptime_title),
            style = textStyle14Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = uptimeText,
                style = CustomTextStyles.textStyle24Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "System runtime",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Version() {
    Text(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        text = stringResource(R.string.app_name),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}

@Composable
@CustomPreview
private fun StatusDataScreenPreview() {

    val networkStateMock = NetworkState(
        statusRobotClient = ConnectionState(
            status = StatusConnection.CONNECTED,
            addressIp = "255.255.255.254"
        ),
        statusRaspiClient = ConnectionState(
            status = StatusConnection.CONNECTING,
            addressIp = "255.255.255.255"
        ),
        localIp = "192.168.0.0"
    )
    val telemetryData = TelemetryData(
        type = "telemetry",
        cpu = CpuData(
            usage = 0F,
            perCore = listOf(),
            temp = 25F
        ),
        fans = mapOf(Pair("cpu", 2352)),
        memory = MemoryData(
            usage = 0F,
            totalMb = 0,
            availableMb = 0
        ),
        disk = DiskData(
            usage = 0F,
            freeGb = 0f,
            totalGb = 0f
        ),
        uptime = 0L
    )
    val mockPreviewDynamicData = RobotDynamicData(
        batVoltage = 39.2f,
        isCharging = true,
        statusCode = StatusRobot.STABILIZED,
        temperatures = Temperatures(
            tempMainboard = 0F,
            tempMcb = 50F,
            tempImu = 80F
        ),
        speedR = 0f,
        speedL = 0f,
        poswheelR = 0f,
        posWheelL = 0f,
        pitchAngle = 0f,
        rollAngle = 0f,
        yawAngle = 0f,
        collisionSensors = CollisionSensors(),
        posInMeters = 0f,
        outputYawControl = 0f,
        currentR = 10f,
        currentL = 7f,
        setPointPos = 0f,
        setPointYaw = 0f,
        setPointAngle = 0f,
        setPointSpeed = 0f
    )

    MyAppTheme {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            StatusDataScreen(
                telemetryState = telemetryData,
                networkState = networkStateMock,
                robotDynamicData = mockPreviewDynamicData,
                onOpenNetworkSettings = {},
                onShutdownAPS = {}
            )
        }
    }
}