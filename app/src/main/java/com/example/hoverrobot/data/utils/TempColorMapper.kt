package com.example.hoverrobot.data.utils

import androidx.compose.ui.graphics.Color

fun Float.mapTempToColor(): Color {
        return when {
            this == 0.0F -> Color.Gray
            this < 10.0 -> Color.Blue
            this < 45.0 -> Color.White
            this < 55.0 -> Color.Yellow
            else -> Color.Red
        }
    }
