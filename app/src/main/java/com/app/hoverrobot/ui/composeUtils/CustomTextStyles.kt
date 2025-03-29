package com.app.hoverrobot.ui.composeUtils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object CustomTextStyles {
    val textStyle16Bold = TextStyle(
        fontSize = 16.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    val textStyle14Bold = TextStyle(
        fontSize = 14.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    val textStyle14Normal = TextStyle(
        fontSize = 14.sp,
        color = Color.White,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )

    val textStyle12Normal = TextStyle(
        fontSize = 12.sp,
        color = Color.White,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start
    )
}