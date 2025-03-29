package com.app.hoverrobot.ui.analisisFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.data.utils.StatusMapper.toColor
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.formatMillisToDate
import com.app.hoverrobot.ui.composeUtils.CustomFloatingButton
import kotlin.collections.mutableListOf

@Composable
fun LogScreen(
    modifier: Modifier,
    listOfLogs: MutableList<Triple<Long, StatusRobot, String?>>
) {
    val scrollState = rememberLazyListState()

    Box(
        modifier = modifier
            .border(border = BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(8.dp))
    ) {
        LazyColumn(
            state = scrollState,
            reverseLayout = true, // Lo más nuevo queda abajo
            modifier = Modifier.fillMaxSize()
        ) {
            items(listOfLogs.size) { log ->
                Text(
                    text = buildAnnotatedString {
                        // Fecha y hora en gris
                        withStyle(style = SpanStyle(color = Color.Gray, fontSize = 12.sp)) {
                            append("${listOfLogs[log].first.formatMillisToDate()} ")
                        }
                        // Mensaje en negrita
                        withStyle(
                            style = SpanStyle(
                                color = listOfLogs[log].second.toColor(),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("${listOfLogs[log].second.name} ")
                        }
                        // Descripción en blanco
                        withStyle(style = SpanStyle(color = Color.White)) {
                            listOfLogs[log].third?.let { description ->
                                append("- $description")
                            }
                        }
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        CustomFloatingButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            icon = Icons.Default.Delete,
            color = Color.Red
        ) { listOfLogs.clear() }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun LogScreenPreview() {

    val simulatedLog = remember {
        mutableListOf<Triple<Long, StatusRobot, String?>>(
            Triple(0L, StatusRobot.STABILIZED, null),
            Triple(0L, StatusRobot.ARMED, null),
            Triple(0L, StatusRobot.CHARGING, null),
            Triple(0L, StatusRobot.ERROR_BATTERY, null)
        )
    }
    Column {
        LogScreen(
            modifier = Modifier,
            listOfLogs = simulatedLog
        )
    }
}