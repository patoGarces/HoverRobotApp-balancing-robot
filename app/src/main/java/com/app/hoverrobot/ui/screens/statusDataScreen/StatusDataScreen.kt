package com.app.hoverrobot.ui.screens.statusDataScreen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomSelectorComponent
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.composeUtils.TemperatureComponent
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun StatusDataScreen(
    statusRobot: StatusRobot,
    networkState: NetworkState,
    defaultAggressiveness: Int,
    mainboardTemp: Float,
    motorControllerTemp: Float,
    imuTemp: Float,
    onOpenNetworkSettings: () -> Unit,
    onAggressivenessChange: (Aggressiveness) -> Unit
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

        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TemperatureComponent(title = R.string.title_mainboard_temp, temp = mainboardTemp)

            TemperatureComponent(title = R.string.title_motorboard_temp, temp = motorControllerTemp)

            TemperatureComponent(title = R.string.title_imu_temp, temp = imuTemp)
        }

        Version()
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
            style = CustomTextStyles.textStyle14Bold
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
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = CustomTextStyles.textStyle14Bold
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
    Column(Modifier.padding(bottom = 8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.title_connection_status),
                style = CustomTextStyles.textStyle14Bold,
                textAlign = TextAlign.Start
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

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(R.string.title_connection_local_ip),
                style = CustomTextStyles.textStyle14Normal
            )

            Text(
                text = networkState.localIp.toString(),
                style = CustomTextStyles.textStyle14Normal
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
                text = stringResource(R.string.title_robot_connection_status),
                style = CustomTextStyles.textStyle14Normal
            )

            Text(
                text = networkState.statusRobotClient.addressIp.toString(),
                style = CustomTextStyles.textStyle14Normal
            )

            CustomButton(
                modifier = Modifier.widthIn(min = 100.dp),
                title = stringResource(networkState.statusRobotClient.status.toStringRes()),
                color = networkState.statusRobotClient.status.toColor(),
                onClick = {}
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
                text = stringResource(R.string.title_raspi_connection_status),
                style = CustomTextStyles.textStyle14Normal
            )

            Text(
                text = networkState.statusRaspiClient.addressIp.toString(),
                style = CustomTextStyles.textStyle14Normal
            )

            CustomButton(
                modifier = Modifier.widthIn(min = 100.dp),
                title = stringResource(networkState.statusRaspiClient.status.toStringRes()),
                color = networkState.statusRaspiClient.status.toColor(),
                onClick = {}
            )
        }

        HorizontalDivider(
            Modifier.padding(horizontal = 8.dp),
            thickness = 1.dp
        )
    }
}

@Composable
private fun Version() {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
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
                mainboardTemp = 12.5F,
                motorControllerTemp = 50F,
                imuTemp = 80F,
                onAggressivenessChange = {},
                onOpenNetworkSettings = {}
            )
        }
    }
}