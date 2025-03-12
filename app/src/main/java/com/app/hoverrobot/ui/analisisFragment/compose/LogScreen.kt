package com.app.hoverrobot.ui.analisisFragment.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.hoverrobot.data.utils.StatusMapper.colorFromRes
import com.app.hoverrobot.data.utils.StatusMapper.colorStatusLog
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.data.utils.formatMillisToDate

@Composable
fun LogScreen(
    newStatusRobot: State<StatusRobot?>,
) {
    val listOfLogs = remember { mutableStateListOf<Triple<Long,StatusRobot,String?>>() }
    val scrollState = rememberLazyListState()

    LaunchedEffect(newStatusRobot.value) {
        newStatusRobot.value?.let {
            listOfLogs.add(0, Triple(System.currentTimeMillis(), it, null))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(border = BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(8.dp))
    ) {
        LazyColumn(
            state = scrollState,
            reverseLayout = true, // Lo más nuevo queda abajo
            modifier = Modifier.fillMaxSize()
        ) {
            items(listOfLogs.size) { log ->
                // TODO: migrar el mapper a compose
                val color = LocalContext.current.colorFromRes(listOfLogs[log].second.colorStatusLog())

                Text(
                    text = buildAnnotatedString {
                        // Fecha y hora en gris
                        withStyle(style = SpanStyle(color = Color.Gray, fontSize = 12.sp)) {
                            append("${listOfLogs[log].first.formatMillisToDate()} ")
                        }
                        // Mensaje en negrita
                        withStyle(style = SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
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

        IconButton(
            onClick = { listOfLogs.clear() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .border(width = 2.dp, color = Color.Red, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                tint = Color.White,
                contentDescription = "Clear logs"
            )
        }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun LogScreenPreview() {

    val simulatedLog = remember { mutableStateOf<StatusRobot?>(
            StatusRobot.INIT
        )
    }
    Column {
        LogScreen(newStatusRobot = simulatedLog)
    }
}