package com.example.hoverrobot.ui.navigationFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hoverrobot.R

@Composable
fun NavigationButtons(
    isRobotStabilized: Boolean,
    yawLeftAngle: String,
    yawRightAngle: String,
    onYawLeftClick: () -> Unit,
    onYawRightClick: () -> Unit,
    onDearmedClick: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isRobotStabilized) {
            DearmedButton {
                onDearmedClick()
            }

            Spacer(Modifier.height(16.dp))
        }
        YawControlButtons(
            yawLeftText = yawLeftAngle,
            yawLeftOnClick = onYawLeftClick,
            yawRightText = yawRightAngle,
            yawRightOnClick = onYawRightClick
        )
    }
}

@Composable
private fun DearmedButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp,Color(0xFF009688))
    ) {
        Text(
            text = stringResource(id = R.string.dearmed_button),
            color = Color.White
        )
    }
}

@Composable
private fun YawControlButtons(
    yawLeftText: String,
    yawLeftOnClick: () -> Unit,
    yawRightText: String,
    yawRightOnClick: () -> Unit,
    ) {

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.Transparent),
            onClick = yawLeftOnClick,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp,Color.White)
        ) {
            Text(
                text = yawLeftText,
                color = Color.White
            )
        }

        OutlinedButton(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.Transparent),
            onClick = yawRightOnClick,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp,Color.White)
        ) {
            Text(
                text = yawRightText,
                color = Color.White
            )
        }
    }

}

@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
fun NavigationButtonPreview() {
    Column(
        Modifier
            .width(300.dp)
            .padding(16.dp)
            .background(Color.Black)
    ) {
        NavigationButtons(
            isRobotStabilized = true,
            yawLeftAngle = "12",
            yawRightAngle = "43",
            onYawLeftClick = {},
            onYawRightClick = {}
        ) {  }
    }
}