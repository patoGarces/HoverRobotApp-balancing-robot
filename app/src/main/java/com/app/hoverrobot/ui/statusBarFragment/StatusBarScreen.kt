package com.app.hoverrobot.ui.statusBarFragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles

@Composable
fun StatusBarScreen(
    statusRobot: State<StatusRobot>,
    connectionState: State<ConnectionState>,
    batteryState: State<Battery>,
    tempImu: State<Float>,
    onClickState: () -> Unit
) {
    Row (
        modifier = Modifier.fillMaxWidth().height(35.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.weight(1F).fillMaxHeight(), horizontalArrangement = Arrangement.Start) {
            NetworkIndicators(
                Modifier,
                connectionState
            )
        }

        CustomButton(
            modifier = Modifier.widthIn(min = 200.dp),
            title = stringResource(statusRobot.value.toStringRes(connectionState.value.status)).uppercase(),
            color = statusRobot.value.toColor(connectionState.value.status),
            onClick = onClickState
        )

        Row(Modifier.weight(1F), horizontalArrangement = Arrangement.End) {
            
            TempIndicator(
                modifier = Modifier,
                tempImu = tempImu
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Red
            )

            BatteryIndicator(
                batteryState = batteryState,
                isConnected = connectionState.value.status == StatusConnection.CONNECTED,
                isMcbOff = statusRobot.value == StatusRobot.ERROR_MCB_CONNECTION
            )
        }
    }
}

@Composable
private fun TempIndicator(
    modifier: Modifier,
    tempImu: State<Float>
) {

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_temp),
            modifier = Modifier.fillMaxHeight(),
            tint = Color.White,
            contentDescription = ""
        )

        Text(
            modifier = Modifier,
            text = stringResource(R.string.placeholder_temp,tempImu.value),
            style = CustomTextStyles.textStyleStatusBar
        )
    }
}


@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun StatusBarScreenPreview() {

    val dummyStatusRobot = remember { mutableStateOf(StatusRobot.INIT) }
    val dummyStatusConnection = remember { mutableStateOf(ConnectionState()) }
    val dummyTempImu = remember { mutableFloatStateOf(19.2F) }
    val dummyBattery = remember { mutableStateOf(Battery()) }

    StatusBarScreen(
        statusRobot = dummyStatusRobot,
        connectionState = dummyStatusConnection,
        tempImu = dummyTempImu,
        batteryState = dummyBattery
    ) { }
}