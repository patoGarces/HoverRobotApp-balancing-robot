package com.app.hoverrobot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MyAppTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode) MyDarkColorScheme else MyLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}