package com.app.hoverrobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.ui.navigation.NavigationScreens

@Composable
fun BottomTabBar(
    tabs: List<NavigationScreens>,
    selectedIndex: Int,
    onTabSelected: (NavigationScreens) -> Unit
) {
    TabRow(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .height(35.dp),
        containerColor = Color.Transparent,
        contentColor = Color.Red,
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .height(2.dp)
                    .background(Color.Red)
            )
        },
        divider = {},
        selectedTabIndex = selectedIndex,
    ) {
        tabs.forEachIndexed { index, screen ->
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clickable { onTabSelected(screen) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = screen.label,
                    color = if (index == selectedIndex) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
