package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun CustomSelectorComponent(
    defaultOption: Int,
    options: List<String>,
    selectedColor: Color = Color.Red,
    optionSelected: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(defaultOption) }

    SingleChoiceSegmentedButtonRow(
        Modifier
            .width(300.dp)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = selectedColor,               // Color de fondo cuando está seleccionado
                    inactiveContainerColor = Color.Transparent,         // Color de fondo cuando no está seleccionado
                    activeContentColor = MaterialTheme.colorScheme.onBackground,                   // Color del texto cuando está seleccionado
                    inactiveContentColor = MaterialTheme.colorScheme.onBackground,                 // Color del texto cuando no está seleccionado
                    activeBorderColor = Color.LightGray,
                    inactiveBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    optionSelected(index)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                label = {
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = label,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

@CustomPreviewComponent
@Composable
fun CustomSelectorComponentPreview() {
    MyAppTheme {
        CustomSelectorComponent(
            defaultOption = 0,
            options = listOf("Option1", "Option2", "Option3"),
        ) { }
    }
}