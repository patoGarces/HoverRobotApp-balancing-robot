package com.app.hoverrobot.ui.statusBarScreen

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.hoverrobot.R
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import androidx.compose.ui.platform.LocalContext
import com.app.hoverrobot.ui.statusBarScreen.composables.BatteryIndicator
import com.app.hoverrobot.ui.statusBarScreen.composables.NetworkIndicators

@Composable
fun StatusBarScreen(statusBarViewModel: StatusBarViewModel = hiltViewModel()) {

    val context = LocalContext.current
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier
                .weight(1F)
                .fillMaxHeight(), horizontalArrangement = Arrangement.Start) {
            NetworkIndicators(
                Modifier,
                statusBarViewModel.connectionState
            )
        }

        CustomButton(
            modifier = Modifier.widthIn(min = 200.dp),
            title = stringResource(statusBarViewModel.statusRobot.toStringRes(statusBarViewModel.connectionState.status)).uppercase(),
            color = statusBarViewModel.statusRobot.toColor(statusBarViewModel.connectionState.status),
            onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        )

        Row(Modifier.weight(1F), horizontalArrangement = Arrangement.End) {
            
            TempIndicator(
                modifier = Modifier,
                tempImu = statusBarViewModel.tempImu
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Red
            )

            BatteryIndicator(
                batteryState = statusBarViewModel.battery,
                isConnected = statusBarViewModel.connectionState.status == StatusConnection.CONNECTED,
                isMcbOff = statusBarViewModel.statusRobot == StatusRobot.ERROR_MCB_CONNECTION
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
            tint = Color.White,
            contentDescription = ""
        )

        Text(
            modifier = Modifier,
            text = stringResource(R.string.placeholder_temp,tempImu),
            style = CustomTextStyles.textStyle16Bold
        )
    }
}


@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun StatusBarScreenPreview() {

//    val dummyStatusRobot = remember { mutableStateOf(StatusRobot.INIT) }
//    val dummyStatusConnection = remember { mutableStateOf(ConnectionState()) }
//    val dummyTempImu = remember { mutableFloatStateOf(19.2F) }
//    val dummyBattery = remember { mutableStateOf(Battery()) }

    StatusBarScreen(
//        statusRobot = dummyStatusRobot,
//        connectionState = dummyStatusConnection,
//        tempImu = dummyTempImu,
//        batteryState = dummyBattery
    )
}