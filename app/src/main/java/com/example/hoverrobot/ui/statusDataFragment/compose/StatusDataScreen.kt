package com.example.hoverrobot.ui.statusDataFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoverrobot.R
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.MapperGralStatus
import com.example.hoverrobot.data.utils.StatusEnumGral
import com.example.hoverrobot.data.utils.StatusMapper
import com.example.hoverrobot.data.utils.mapTempToColor

@Composable
fun StatusDataScreen(
    robotStatus: StatusEnumGral,
    connectionStatus: ConnectionStatus,
    defaultAggressiveness: Int,
    mainboardTemp: Float,
    motorControllerTemp: Float,
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
            value = MapperGralStatus(LocalContext.current).mapGralStatusText(robotStatus),
            colorOutline = colorResource(MapperGralStatus(LocalContext.current).mapGralStatusToColor(robotStatus)),
        ) { }

        NormalComponent(
            title = stringResource(R.string.title_connection_status),
            value = StatusMapper.statusToString(connectionStatus),
            colorOutline = colorResource(StatusMapper.statusToColor(connectionStatus))
        ) {
            onNewAction(OnActionStatusDataScreen.OnActionOpenNetworkSettings)
        }

        Row(
            Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TemperatureComponent(
                stringResource(R.string.title_mainboard_temp),
                stringResource(R.string.placeholder_temp).format(mainboardTemp),
                mainboardTemp.mapTempToColor())

            TemperatureComponent(
                stringResource(R.string.title_motorboard_temp),
                stringResource(R.string.placeholder_temp).format(motorControllerTemp),
                motorControllerTemp.mapTempToColor()
            )
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
private fun TemperatureComponent(title: String,temp: String,colorOutline: Color) {
    Column(
        Modifier
            .padding(8.dp)
            .size(120.dp)
            .border(width = 2.dp, color = colorOutline, shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.weight(1F),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Text(
            text = temp,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VersionAndIp(version: String,localIp: String?) {

    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = version + if(localIp != null ) " - Local ip: $localIp" else "",
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
            robotStatus = StatusEnumGral.NORMAL,
            connectionStatus = ConnectionStatus.CONNECTED,
            defaultAggressiveness = 0,
            mainboardTemp = 12.5F,
            motorControllerTemp = 50F,
            version = "V1.2.3",
            localIp = "255.255.255.255"
        ) { }
    }
}

sealed class OnActionStatusDataScreen {
    data class OnAggressivenessChange(val level: Int): OnActionStatusDataScreen()
    data object OnActionOpenNetworkSettings: OnActionStatusDataScreen()
}