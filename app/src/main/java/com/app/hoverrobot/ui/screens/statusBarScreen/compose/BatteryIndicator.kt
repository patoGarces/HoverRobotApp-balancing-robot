package com.app.hoverrobot.ui.screens.statusBarScreen.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.data.models.BATTERY_LEVEL_EMPTY
import com.app.hoverrobot.data.models.BATTERY_LEVEL_HIGH
import com.app.hoverrobot.data.models.BATTERY_LEVEL_LOW
import com.app.hoverrobot.data.models.BATTERY_LEVEL_MEDIUM
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.ui.composeUtils.CustomColors
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle16Bold

val Float.inNormalRange: Boolean
    get() = this in 30F..50F

@Composable
fun BatteryIndicator(
    batteryState: Battery,
    isConnected: Boolean,
    isMcbOff: Boolean
) {

    val batteryVoltageText = if (batteryState.voltage.inNormalRange && isConnected && !isMcbOff)
        stringResource(R.string.placeholder_battery_voltage, batteryState.voltage)
    else stringResource(R.string.variable_not_available)

    val batteryPercentText = if (isConnected && !isMcbOff)
        stringResource(R.string.placeholder_battery_percent, batteryState.level)
    else stringResource(R.string.variable_not_available)

    var iconTint by remember { mutableStateOf(Color.White) }

    val icon = when {
        !isConnected || isMcbOff -> {
            iconTint = Color.White
            R.drawable.ic_battery_unknown
        }

        batteryState.isCharging -> {
            iconTint = Color.White
            R.drawable.ic_battery_charging
        }

        batteryState.level > BATTERY_LEVEL_HIGH -> {
            iconTint = Color.White
            R.drawable.ic_battery_4
        }

        batteryState.level > BATTERY_LEVEL_MEDIUM -> {
            iconTint = Color.White
            R.drawable.ic_battery_3
        }

        batteryState.level > BATTERY_LEVEL_LOW -> {
            iconTint = Color.White
            R.drawable.ic_battery_2
        }

        batteryState.level > BATTERY_LEVEL_EMPTY -> {
            iconTint = CustomColors.StatusOrange
            R.drawable.ic_battery_1
        }

        else -> {
            iconTint = CustomColors.StatusRed
            R.drawable.ic_battery_0
        }
    }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = batteryVoltageText,
            style = textStyle16Bold
        )

        Icon(
            painter = painterResource(id = icon),
            modifier = Modifier
                .size(48.dp)
                .padding(horizontal = 8.dp),
            tint = iconTint,
            contentDescription = ""
        )

        Text(
            text = batteryPercentText,
            style = textStyle16Bold
        )
    }
}

@Preview
@Composable
fun BatteryIndicatorPReview() {
    Column {
        BatteryIndicator(
            batteryState = Battery(false,10,40.3F),
            isConnected = true,
            isMcbOff = false
        )

        BatteryIndicator(
            batteryState = Battery(false,20,40.3F),
            isConnected = true,
            isMcbOff = false
        )

        BatteryIndicator(
            batteryState = Battery(false,30,40.3F),
            isConnected = true,
            isMcbOff = false
        )

        BatteryIndicator(
            batteryState = Battery(false,100,40.3F),
            isConnected = true,
            isMcbOff = false
        )

        BatteryIndicator(
            batteryState = Battery(false,50,40F),
            isConnected = false,
            isMcbOff = false
        )

        BatteryIndicator(
            batteryState = Battery(true,20,40.3F),
            isConnected = true,
            isMcbOff = false
        )
    }
}
