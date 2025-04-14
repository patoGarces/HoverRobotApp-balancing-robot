package com.app.hoverrobot.ui.composeUtils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object CustomTextStyles {
    val textStyle16Bold: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )


    val textStyle14Bold: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

    val textStyle14Normal: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
}