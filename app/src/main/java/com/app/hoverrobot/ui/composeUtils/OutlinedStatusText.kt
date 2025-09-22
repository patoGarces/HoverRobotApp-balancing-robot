package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Bold
import com.app.hoverrobot.ui.composeUtils.CustomTextStyles.textStyle14Normal

@Composable
fun OutlinedStatusText(
    modifier: Modifier,
    title: String,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(modifier) {
        Text(
            modifier = Modifier
                .widthIn(min = 140.dp)
                .border(width = 1.dp, color = color, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.CenterEnd),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = if (isBold) textStyle14Bold else textStyle14Normal,
            textAlign = TextAlign.Center
        )
    }
}