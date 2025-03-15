package com.app.hoverrobot.ui.statusDataScreen

import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.hoverrobot.BuildConfig
import com.app.hoverrobot.R
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusMapper.toStringRes
import com.app.hoverrobot.ui.composeUtils.CustomButton
import com.app.hoverrobot.ui.composeUtils.CustomSelectorComponent
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles
import com.app.hoverrobot.ui.composeUtils.TemperatureComponent
import com.app.hoverrobot.ui.navigationFragment.NavigationViewModel

@Composable
fun StatusDataScreen(
    statusDataViewModel: StatusDataViewModel = hiltViewModel(),
    navigationViewModel: NavigationViewModel = hiltViewModel(),
) {

    
    /*
     * TODO: pendientes:
     *  - Lllamar a startActivity
     * - Bug en temperaturas: nunca son null, y tampoco vuelven a null al desconectarse
     * - migrar a mutable state el aggresivenessLevel
     */

    val context = LocalContext.current
    val defaultAggressiveness = navigationViewModel.aggressivenessLevel.observeAsState(0).value

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleScreen(stringResource(R.string.title_status_fragment))

        val options = listOf("Suave", "Moderado", "Agresivo")
        SelectorSection (
            title = stringResource(R.string.title_aggressiveness),
            defaultOption = defaultAggressiveness,
            options = options
        ) { optionSelected ->
            navigationViewModel.setLevelAggressiveness(optionSelected)
        }

        NormalSection(
            title = R.string.title_status_robot,
            buttonText = statusDataViewModel.gralStatus.toStringRes(statusDataViewModel.statusConnection),
            colorOutline = statusDataViewModel.gralStatus.toColor(statusDataViewModel.statusConnection),
        ) { }

        NormalSection(
            title = R.string.title_connection_status,
            buttonText = statusDataViewModel.statusConnection.toStringRes(),
            colorOutline = statusDataViewModel.statusConnection.toColor()
        ) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TemperatureComponent(
                R.string.title_mainboard_temp,
                statusDataViewModel.mainboardTemp
            )

            TemperatureComponent(
                R.string.title_motorboard_temp,
                statusDataViewModel.motorControllerTemp
            )

            TemperatureComponent(
                R.string.title_imu_temp,
                statusDataViewModel.imuTemp
            )
        }

        VersionAndIp(
            version = stringResource(R.string.version_placeholder,BuildConfig.VERSION_NAME),
            localIp = statusDataViewModel.localIp
        )
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
            color = Color.White,
            style = CustomTextStyles.textStyle14Normal
        )

        CustomButton(
            title = stringResource(buttonText),
            modifier = Modifier.widthIn(min = 200.dp),
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
            style = CustomTextStyles.textStyle14Normal
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
private fun VersionAndIp(version: String, localIp: String?) {

    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = version + if (!localIp.isNullOrEmpty()) " - Local ip: $localIp" else "",
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
//            statusRobot = StatusRobot.STABILIZED,
//            statusConnection = StatusConnection.CONNECTED,
//            defaultAggressiveness = 0,
//            mainboardTemp = 12.5F,
//            motorControllerTemp = 50F,
//            imuTemp = 80F,
//            version = "V1.2.3",
//            localIp = "255.255.255.255",
//            onAggressivenessChange = {},
//            onOpenNetworkSettings = {}
        )
    }
}