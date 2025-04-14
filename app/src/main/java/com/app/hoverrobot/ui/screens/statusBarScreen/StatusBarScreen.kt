package com.app.hoverrobot.ui.screens.statusBarScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
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
    connectionState: ConnectionState,
    batteryState: Battery,
    tempImu: Float,
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
                connectionState
            )
        }

        CustomButton(
            modifier = Modifier.widthIn(min = 200.dp),
            title = stringResource(statusRobot.toStringRes(connectionState.status)).uppercase(),
            color = statusRobot.toColor(connectionState.status),
            onClick = onClickBtnStatus
        )

        Row(Modifier.weight(1F), horizontalArrangement = Arrangement.End) {

            TempIndicator(
                modifier = Modifier,
                tempImu = tempImu
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )

            BatteryIndicator(
                batteryState = batteryState,
                isConnected = connectionState.status == StatusConnection.CONNECTED,
                isMcbOff = statusRobot == StatusRobot.ERROR_MCB_CONNECTION
            )
        }
    }
}

@Composable
private fun TempIndicator(
    modifier: Modifier,
    tempImu: Float
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_temp),
            modifier = Modifier.fillMaxHeight(),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = ""
        )

        Text(
            modifier = Modifier,
            text = stringResource(R.string.placeholder_temp, tempImu),
            style = CustomTextStyles.textStyle16Bold,
        )
    }
}

@CustomPreview
@Composable
fun StatusBarScreenPreview() {

    MyAppTheme {
        StatusBarScreen(
            statusRobot = StatusRobot.INIT,
            connectionState = ConnectionState(),
            tempImu = 23F,
            batteryState = Battery()
        ) { }
    }
}