package com.app.hoverrobot.ui.screens.statusBarScreen.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkWifi1Bar
import androidx.compose.material.icons.outlined.NetworkWifi2Bar
import androidx.compose.material.icons.outlined.NetworkWifi3Bar
import androidx.compose.material.icons.outlined.SignalWifi0Bar
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material.icons.outlined.Wifi
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
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.models.comms.NetworkState
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.ui.composeUtils.CustomPreviewComponent
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun NetworkIndicators(
    modifier: Modifier,
    networkState: NetworkState
) {
    val icon = when(networkState.strength) {
        0 -> Icons.Outlined.SignalWifi0Bar
        1 -> Icons.Outlined.NetworkWifi1Bar
        2 -> Icons.Outlined.NetworkWifi2Bar
        3 -> Icons.Outlined.NetworkWifi3Bar
        4 -> Icons.Outlined.Wifi
        else -> Icons.Outlined.SignalWifiOff
    }

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            modifier = Modifier.fillMaxHeight(),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = ""
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.placeholder_rssi,networkState.rssi),
            style = CustomTextStyles.textStyle14Bold
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.placeholder_packets_rate,networkState.statusRobotClient.receiverPacketRates),
            style = CustomTextStyles.textStyle14Bold
        )
    }
}

@CustomPreviewComponent
@Composable
private fun NetworkIndicatorsPreview() {
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
        Column {
            Box(Modifier.height(40.dp)) {
                NetworkIndicators(
                    Modifier.height(35.dp),
                    networkState = networkStateMock.copy(strength = 0)
                )
            }
            Box(Modifier.height(40.dp)) {
                NetworkIndicators(
                    Modifier.height(35.dp),
                    networkState = networkStateMock.copy(strength = 1)
                )
            }
            Box(Modifier.height(40.dp)) {
                NetworkIndicators(
                    Modifier.height(35.dp),
                    networkState = networkStateMock.copy(strength = 2)
                )
            }
            Box(Modifier.height(40.dp)) {
                NetworkIndicators(
                    Modifier.height(35.dp),
                    networkState = networkStateMock.copy(strength = 3)
                )
            }
            Box(Modifier.height(40.dp)) {
                NetworkIndicators(
                    Modifier.height(35.dp),
                    networkState = networkStateMock.copy(strength = 4)
                )
            }
        }
    }
}