package com.app.hoverrobot.ui.screens.statusBarScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.screens.statusBarScreen.compose.BatteryIndicator
import com.app.hoverrobot.ui.screens.statusBarScreen.compose.NetworkIndicators
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun StatusBarScreen(
    statusRobot: StatusRobot,
    networkState: NetworkState,
    batteryState: Battery,
    cpuUsage: Float,
    temperature: Float,
    onClickBtnStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier
                .weight(1F)
                .fillMaxHeight(), horizontalArrangement = Arrangement.Start
        ) {
            NetworkIndicators(
                Modifier,
                networkState
            )
        }

        CustomButton(
            modifier = Modifier.widthIn(min = 200.dp),
            title = stringResource(statusRobot.toStringRes(networkState.statusRobotClient.status)).uppercase(),
            color = statusRobot.toColor(networkState.statusRobotClient.status),
            onClick = onClickBtnStatus
        )

        Row(Modifier.weight(1F), horizontalArrangement = Arrangement.End) {

            CpuUsagePercent(
                modifier = Modifier,
                usage = cpuUsage.takeIf { networkState.statusRaspiClient.status == StatusConnection.CONNECTED }
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .padding(start = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )

            TempIndicator(
                modifier = Modifier,
                temperature = temperature.takeIf { networkState.statusRaspiClient.status == StatusConnection.CONNECTED }
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .padding(start = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )

            BatteryIndicator(
                batteryState = batteryState,
                isConnected = networkState.statusRobotClient.status == StatusConnection.CONNECTED,
                isMcbOff = statusRobot == StatusRobot.ERROR_MCB_CONNECTION
            )
        }
    }
}

@Composable
private fun TempIndicator(
    modifier: Modifier,
    temperature: Float?
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Outlined.Thermostat,
            modifier = Modifier.fillMaxHeight(),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = ""
        )

        val tempText = temperature?.let { stringResource(R.string.placeholder_temp, temperature) } ?:
            stringResource(R.string.variable_not_available)
        Text(
            modifier = Modifier,
            text = tempText,
            style = CustomTextStyles.textStyle14Bold,
        )
    }
}

@Composable
private fun CpuUsagePercent(
    modifier: Modifier,
    usage: Float?
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Memory,
            modifier = Modifier.fillMaxHeight(),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = "CPU usage"
        )

        val textUsage = usage?.let { stringResource(R.string.placeholder_cpu_usage, usage) } ?:
                stringResource(R.string.variable_not_available)
        Text(
            text = textUsage,
            style = CustomTextStyles.textStyle14Bold,
        )
    }
}

@CustomPreview
@Composable
fun StatusBarScreenPreview() {
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

    MyAppTheme {
        StatusBarScreen(
            statusRobot = StatusRobot.INIT,
            networkState = networkStateMock,
            cpuUsage = 12.2F,
            temperature = 23F,
            batteryState = Battery()
        ) { }
    }
}