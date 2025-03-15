package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun CustomFloatingButton(
    modifier: Modifier,
    icon: ImageVector,
    color: Color = CustomColors.PurpleThemeDefault,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 2.dp,
                color = color,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            tint = Color.White,
            contentDescription = "Clear logs"
        )
    }
}

@Preview
@Composable
private fun CustomFloatingButtonPreview() {
    CustomFloatingButton(
        modifier = Modifier,
        icon = Icons.Filled.PlayArrow,
    ) { }
}