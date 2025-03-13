package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R

@Composable
fun CustomOutlinedButton(
    @StringRes title: Int,
    height: Dp = 50.dp,
    enable: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .height(35.dp)
            .background(Color.Transparent)
            .padding(horizontal = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Red),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        AnimatedContent(
            targetState = isLoading, label = ""
        ) { state ->
            if (state) {
                Column(
                    modifier = Modifier.widthIn(min = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        Modifier.size(30.dp),
                        color = Color.Red,
                        strokeWidth = 3.dp
                    )
                }
            } else {
                Text(
                    text = stringResource(title),
                    color = if (enable) Color.White else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun OutlinedButtonPreview() {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CustomOutlinedButton(
            title = R.string.btn_clear_title
        ) { }

        CustomOutlinedButton(
            title = R.string.btn_pid_sync,
            enable = false
        ) { }

        CustomOutlinedButton(
            title = R.string.btn_clear_title,
            isLoading = true
        ) { }
    }
}