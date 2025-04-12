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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun CustomButton(
    title: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 35.dp,
    enable: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (enable) MaterialTheme.colorScheme.onPrimary else Color.Gray
    OutlinedButton(
        modifier = modifier
            .widthIn(min = 100.dp)
            .height(height),
        onClick = onClick,
        enabled = enable,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filled) color else Color.Transparent,
            contentColor = contentColor
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
                    text = title,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun CustomButtonPreview() {

    MyAppTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CustomButton(
                title = "Clear data"
            ) { }

            Spacer(Modifier.height(8.dp))

            CustomButton(
                title = "Clear data",
                filled = true
            ) { }

            Spacer(Modifier.height(8.dp))


            CustomButton(
                title = "Sync(Disabled)",
                enable = false
            ) { }

            Spacer(Modifier.height(8.dp))

            CustomButton(
                title = "Clear data",
                isLoading = true
            ) { }
        }
    }
}