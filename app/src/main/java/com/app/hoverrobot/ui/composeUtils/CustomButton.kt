package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
fun CustomButton(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    color: Color = Color.Red,
    height: Dp = 35.dp,
    enable: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .widthIn(min = 100.dp)
            .height(height),
        onClick = onClick,
        enabled = enable,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filled) color else Color.Transparent
        ),
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
                        color = color,
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
private fun CustomButtonPreview() {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CustomButton(
            title = R.string.btn_clear_title
        ) { }

        Spacer(Modifier.height(8.dp))

        CustomButton(
            title = R.string.btn_clear_title,
            filled = true
        ) { }

        Spacer(Modifier.height(8.dp))


        CustomButton(
            title = R.string.btn_pid_sync,
            enable = false
        ) { }

        Spacer(Modifier.height(8.dp))


        CustomButton(
            title = R.string.btn_clear_title,
            isLoading = true
        ) { }
    }
}