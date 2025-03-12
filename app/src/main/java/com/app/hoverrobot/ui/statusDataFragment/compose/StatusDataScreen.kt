package com.app.hoverrobot.ui.statusDataFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusMapper.colorRes
import com.app.hoverrobot.data.utils.StatusMapper.stringRes
import com.app.hoverrobot.data.utils.StatusRobot

@Composable
fun StatusDataScreen(
    statusRobot: StatusRobot,
    statusConnection: StatusConnection,
    defaultAggressiveness: Int,
    mainboardTemp: Float,
    motorControllerTemp: Float,
    imuTemp: Float,
    version: String,
    localIp: String?,
    onNewAction: (OnActionStatusDataScreen) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleScreen(stringResource(R.string.title_status_fragment))

        val options = listOf("Suave","Moderado","Agresivo")
        SelectorComponent(
            stringResource(R.string.title_aggressiveness),
            defaultAggressiveness,
            options
        ) { optionSelected ->
            onNewAction(OnActionStatusDataScreen.OnAggressivenessChange(optionSelected))
        }

        NormalComponent(
            title = stringResource(R.string.title_status_robot),
            value = stringResource(statusRobot.stringRes(statusConnection)),
            colorOutline = colorResource(statusRobot.colorRes(statusConnection)),
        ) { }

        NormalComponent(
            title = stringResource(R.string.title_connection_status),
            value = stringResource(statusConnection.stringRes()),
            colorOutline = colorResource(statusConnection.colorRes())
        ) {
            onNewAction(OnActionStatusDataScreen.OnActionOpenNetworkSettings)
        }

        Row(
            Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (mainboardTemp != 0.0F) {
                TemperatureComponent(
                    stringResource(R.string.title_mainboard_temp),
                    mainboardTemp
                )
            }

            if (motorControllerTemp != 0.0F) {
                TemperatureComponent(
                    stringResource(R.string.title_motorboard_temp),
                    motorControllerTemp
                )
            }

            if (imuTemp != 0.0F) {
                TemperatureComponent(
                    stringResource(R.string.title_imu_temp),
                    imuTemp
                )
            }
        }

        VersionAndIp(version,localIp)
    }
}

@Composable
private fun TitleScreen(text: String) {

    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        fontSize = 24.sp,
        color = Color.White
    )

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun SelectorComponent(
    title: String,
    defaultOption: Int,
    options: List<String>,
    optionSelected: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(defaultOption) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White
        )

        SingleChoiceSegmentedButtonRow(
            Modifier.width(280.dp)
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Color.Red,               // Color de fondo cuando está seleccionado
                        inactiveContainerColor = Color.Transparent,     // Color de fondo cuando no está seleccionado
                        activeContentColor = Color.White,               // Color del texto cuando está seleccionado
                        inactiveContentColor = Color.White              // Color del texto cuando no está seleccionado
                    ),
                    selected = selectedIndex == index,
                    onClick = {
                        selectedIndex = index
                        optionSelected(index)
                              },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    label = {
                        Text(
                            color = Color.White,
                            text = label,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun NormalComponent(
    title: String,
    value: String,
    colorOutline: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White
        )

        Button(
            modifier = Modifier.background(Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(2.dp,colorOutline),
            onClick = onClick,
        ) {
            Text(
                text = value,
                fontSize = 14.sp
            )
        }
    }

    HorizontalDivider(
        Modifier.padding(horizontal = 8.dp),
        thickness = 1.dp
    )
}

@Composable
private fun VersionAndIp(version: String,localIp: String?) {

    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = version + if(!localIp.isNullOrEmpty() ) " - Local ip: $localIp" else "",
        fontSize = 14.sp,
        color = Color.White
    )
}

@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
private fun AggressivenessScreenPreview() {

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        StatusDataScreen(
            statusRobot = StatusRobot.STABILIZED,
            statusConnection = StatusConnection.CONNECTED,
            defaultAggressiveness = 0,
            mainboardTemp = 12.5F,
            motorControllerTemp = 50F,
            imuTemp = 80F,
            version = "V1.2.3",
            localIp = "255.255.255.255"
        ) { }
    }
}

sealed class OnActionStatusDataScreen {
    data class OnAggressivenessChange(val level: Int): OnActionStatusDataScreen()
    data object OnActionOpenNetworkSettings: OnActionStatusDataScreen()
}