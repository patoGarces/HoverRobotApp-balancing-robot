package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
fun OutlinedButton(
    @StringRes title: Int,
    height: Dp = 50.dp,
    enable: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .height(height)
            .padding(8.dp)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.Red
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enable,
            modifier = Modifier.fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp)
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
}

@Preview
@Composable
private fun OutlinedButtonPreview() {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(
            title = R.string.btn_clear_title
        ) { }

        OutlinedButton(
            title = R.string.btn_clear_title,
            enable = false
        ) { }

        OutlinedButton(
            title = R.string.btn_clear_title,
            isLoading = true
        ) { }
    }
}