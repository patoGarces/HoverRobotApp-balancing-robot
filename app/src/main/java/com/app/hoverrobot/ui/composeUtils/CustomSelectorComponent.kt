package com.app.hoverrobot.ui.composeUtils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .width(280.dp)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = selectedColor,               // Color de fondo cuando está seleccionado
                    inactiveContainerColor = Color.Transparent,     // Color de fondo cuando no está seleccionado
                    activeContentColor = Color.White,               // Color del texto cuando está seleccionado
                    inactiveContentColor = Color.White,              // Color del texto cuando no está seleccionado
                    activeBorderColor = Color.Red,
                    inactiveBorderColor = Color.White
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
                        color = Color.White,
                        text = label,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}


@Preview
@Composable
fun CustomSelectorComponentPreview() {

    Column(Modifier.fillMaxSize()) {
        CustomSelectorComponent(
            defaultOption = 0,
            options = listOf("Option1", "Option2", "Option3"),
        ) { }
    }
}