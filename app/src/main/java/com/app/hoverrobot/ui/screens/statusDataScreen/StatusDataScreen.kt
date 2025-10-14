package com.app.hoverrobot.ui.screens.statusDataScreen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.Aggressiveness
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.models.comms.Temperatures
import com.app.hoverrobot.data.repositories.IP_ADDRESS_CLIENT_NULL
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomSelectorComponent
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Bold
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Normal
import com.app.hoverrobot.ui.composeUtils.OutlinedStatusText
import com.app.hoverrobot.ui.composeUtils.TemperatureComponent
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun StatusDataScreen(
    statusRobot: StatusRobot,
    networkState: NetworkState,
    defaultAggressiveness: Int,
    temperatures: Temperatures,
    onOpenNetworkSettings: () -> Unit,
    onAggressivenessChange: (Aggressiveness) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TitleScreen(stringResource(R.string.title_status_screen))

            val options = listOf("Suave", "Moderado", "Agresivo")
            SelectorSection(
                title = stringResource(R.string.title_aggressiveness),
                defaultOption = defaultAggressiveness,
                options = options
            ) { optionSelected ->
                onAggressivenessChange(Aggressiveness.entries[optionSelected])
            }

            NormalSection(
                title = R.string.title_status_robot,
                buttonText = statusRobot.toStringRes(networkState.statusRobotClient.status),
                colorOutline = statusRobot.toColor(networkState.statusRobotClient.status),
            ) { }

            ConnectionSection(
                networkState = networkState,
                onOpenNetworkSettings = onOpenNetworkSettings
            )

            TemperatureSection(temperatures)
        }

        Version(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun TitleScreen(text: String) {

    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.onBackground
    )

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun NormalSection(
    @StringRes title: Int,
    @StringRes buttonText: Int,
    colorOutline: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(title),
            style = textStyle14Bold
        )

        CustomButton(
            title = stringResource(buttonText),
            modifier = Modifier.widthIn(min = 100.dp),
            color = colorOutline,
            onClick = onClick
        )
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
fun SelectorSection(
    title: String,
    defaultOption: Int,
    options: List<String>,
    optionSelected: (Int) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = textStyle14Bold
        )

        CustomSelectorComponent(
            defaultOption = defaultOption,
            options = options,
            optionSelected = optionSelected,
        )
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun ConnectionSection(
    networkState: NetworkState,
    onOpenNetworkSettings: () -> Unit
) {
    var isContentExpanded by remember { mutableStateOf(true) }

    Column(Modifier.padding(bottom = 8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isContentExpanded = !isContentExpanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1F),
                text = stringResource(R.string.title_connection_status),
                style = textStyle14Bold,
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(30.dp)
                    .clickable { isContentExpanded = !isContentExpanded},
                contentAlignment = Alignment.Center
            ) {
                val icon = if (isContentExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        AnimatedVisibility(
            visible = isContentExpanded,
        ) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(2F),
                        text = stringResource(R.string.title_connection_local_ip),
                        textAlign = TextAlign.Start,
                        style = textStyle14Normal
                    )

                    Text(
                        modifier = Modifier.weight(2F),
                        text = networkState.localIp?.toString()
                            ?: stringResource(R.string.unknown_ip),
                        style = textStyle14Normal
                    )

                    Box(
                        modifier = Modifier
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

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(2F),
                        text = stringResource(R.string.title_robot_connection_status),
                        textAlign = TextAlign.Start,
                        style = textStyle14Normal
                    )

                    OutlinedStatusText(
                        modifier = Modifier.weight(1F),
                        title = networkState.statusRobotClient.addressIp ?: IP_ADDRESS_CLIENT_NULL,
                        color = Color.White
                    )

                    OutlinedStatusText(
                        modifier = Modifier.weight(1F),
                        title = stringResource(networkState.statusRobotClient.status.toStringRes()),
                        color = networkState.statusRobotClient.status.toColor(),
                        isBold = true
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(2F),
                        text = stringResource(R.string.title_raspi_connection_status),
                        textAlign = TextAlign.Start,
                        style = textStyle14Normal
                    )

                    OutlinedStatusText(
                        modifier = Modifier.weight(1F),
                        title = networkState.statusRaspiClient.addressIp ?: IP_ADDRESS_CLIENT_NULL,
                        color = Color.White
                    )

                    OutlinedStatusText(
                        modifier = Modifier.weight(1F),
                        title = stringResource(networkState.statusRaspiClient.status.toStringRes()),
                        color = networkState.statusRaspiClient.status.toColor(),
                    )
                }
            }
        }

        HorizontalDivider(
            Modifier.padding(horizontal = 8.dp),
            thickness = 1.dp
        )
    }
}

@Composable
private fun TemperatureSection(
    temperatures: Temperatures
) {
    var isContentExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isContentExpanded = !isContentExpanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1F),
                text = stringResource(R.string.title_temperature_status),
                style = textStyle14Bold,
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(30.dp)
                    .clickable { isContentExpanded = !isContentExpanded },
                contentAlignment = Alignment.Center
            ) {
                val icon = if (isContentExpanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        AnimatedVisibility(
            visible = isContentExpanded
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TemperatureComponent(
                    title = R.string.title_mainboard_temp,
                    temp = temperatures.tempMainboard
                )

                TemperatureComponent(
                    title = R.string.title_motorboard_temp,
                    temp = temperatures.tempMcb
                )

                TemperatureComponent(
                    title = R.string.title_imu_temp,
                    temp = temperatures.tempImu
                )
            }
        }
    }
}

@Composable
private fun Version(modifier: Modifier) {
    Text(
        modifier = modifier.padding(vertical = 4.dp),
        text = stringResource(R.string.app_name),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
@CustomPreview
private fun AggressivenessScreenPreview() {

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
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            StatusDataScreen(
                statusRobot = StatusRobot.STABILIZED,
                networkState = networkStateMock,
                defaultAggressiveness = 0,
                temperatures = Temperatures(
                    tempMainboard = 12.5F,
                    tempMcb = 50F,
                    tempImu = 80F
                ),
                onAggressivenessChange = {},
                onOpenNetworkSettings = {}
            )
        }
    }
}