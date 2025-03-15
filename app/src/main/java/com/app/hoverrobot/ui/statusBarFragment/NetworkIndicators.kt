package com.app.hoverrobot.ui.statusBarFragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.utils.ImagebuttonsMappers.strengthIconMapper
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle16Bold

@Composable
fun NetworkIndicators(
    modifier: Modifier,
    connectionState: State<ConnectionState>
) {

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = strengthIconMapper(connectionState.value.strength)),
            modifier = Modifier.fillMaxHeight(),
            tint = Color.White,
            contentDescription = ""
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.placeholder_rssi,connectionState.value.rssi),
            style = textStyle16Bold
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(8.dp),
            thickness = 1.dp,
            color = Color.Red
        )

        Text(
            text = stringResource(R.string.placeholder_packets_rate,connectionState.value.receiverPacketRates),
            style = textStyle16Bold
        )
    }
}

@Preview
@Composable
private fun NetworkIndicatorsPreview() {

    val dummyConnectionState = remember { mutableStateOf(ConnectionState()) }
    Column {
        NetworkIndicators(
            Modifier.height(35.dp),
            connectionState = dummyConnectionState
        )
    }
}