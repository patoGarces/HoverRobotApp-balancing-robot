package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.ArrayRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun CustomDropdownMenu(
    @ArrayRes options: Int,
    actualIndex: Int,
    initialExpanded: Boolean = false,
    onIndexChange: (Int) -> Unit
) {
    val optionDropDownMenu = stringArrayResource(options)
    val listColor = listOf(
        MaterialTheme.colorScheme.onBackground,
        Color.Blue,
        Color.Green,
        Color.Yellow
    )

    var isDropdownMenuExpanded by remember { mutableStateOf(initialExpanded) }

    val outlineColor = listColor[actualIndex]

    Box(
        modifier = Modifier
            .width(130.dp)
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .clickable { isDropdownMenuExpanded = !isDropdownMenuExpanded }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = optionDropDownMenu.getOrNull(actualIndex) ?: "Unknown",
                style = CustomTextStyles.textStyle14Bold
            )
            val trailingIcon =
                if (isDropdownMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            Icon(
                imageVector = trailingIcon,
                contentDescription = "Dropdown",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        DropdownMenu(
            expanded = isDropdownMenuExpanded,
            onDismissRequest = { isDropdownMenuExpanded = false },
            modifier = Modifier
                .width(130.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            optionDropDownMenu.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(item, color = MaterialTheme.colorScheme.onSurface)
                    },
                    onClick = {
                        onIndexChange(index)
                        isDropdownMenuExpanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}


@Composable
@CustomPreviewComponent
private fun CustomDropdownMenuPreview() {
    MyAppTheme {
        CustomDropdownMenu(
            options = R.array.dropdown_menu_pid_items,
            actualIndex = 0,
            initialExpanded = true
        ) { }
    }
}