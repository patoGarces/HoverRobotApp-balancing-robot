package com.app.hoverrobot.ui.composeUtils

import android.util.Log
import android.widget.NumberPicker
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.getColor
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.theme.MyAppTheme

@Composable
fun NumberPickerDialog(
    initialValue: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val context = LocalContext.current
    val numberPicker = remember {
        NumberPicker(context).apply {
            minValue = range.start
            maxValue = range.endInclusive
            value = initialValue
            wrapSelectorWheel = false
        }
    }

    AlertDialog(
        containerColor = Color.DarkGray,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(numberPicker.value) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            AndroidView({ numberPicker })
        }
    )
}

@Composable
fun StepperDistanceDialog(
    initialDistance: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var distance by remember { mutableStateOf(initialDistance) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(distance) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$distance cm", fontSize = 24.sp)
                Row {
                    Button(onClick = { distance = (distance - 10).coerceAtLeast(10) }) {
                        Text("-10")
                    }
                    Button(onClick = { distance = (distance - 1).coerceAtLeast(10) }) {
                        Text("-1")
                    }
                    Button(onClick = { distance = (distance + 1).coerceAtMost(500) }) {
                        Text("+1")
                    }
                    Button(onClick = { distance = (distance + 10).coerceAtMost(500) }) {
                        Text("+10")
                    }
                }
            }
        }
    )
}

@Composable
fun SwipeNumberDialog(
    initialDistance: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var distance by remember { mutableIntStateOf(initialDistance) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(distance) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            distance = (distance - (dragAmount / 5).toInt()).coerceIn(10, 500)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("$distance cm", fontSize = 32.sp)
            }
        }
    )
}

//@Composable
//fun DistancePickerDialog(
//    initialMeters: Int,
//    initialDecimeters: Int,
//    onConfirm: (Float) -> Unit,
//    onDismiss: () -> Unit
//) {
//    var selectedMeters by remember { mutableIntStateOf(initialMeters) }
//    var selectedDecimeters by remember { mutableIntStateOf(initialDecimeters) }
//    var actualDistance by remember { mutableFloatStateOf(0F) }
//
//    LaunchedEffect(selectedMeters,selectedDecimeters) {
//        actualDistance = selectedMeters.times(100).toFloat() + selectedDecimeters.times(10)
//    }
//
//    AlertDialog(
//        modifier = Modifier.border(1.dp, color = Color.Red, shape = RoundedCornerShape(16.dp)),
//        shape = RoundedCornerShape(16.dp),
//        onDismissRequest = { onDismiss() },
//        title = {
//            Text(
//                text = "Distancia",
//                modifier = Modifier.width(230.dp),
//                textAlign = TextAlign.Center
//            )
//        },
//        containerColor = Color.DarkGray,
//        text = {
//            Row(
//                modifier = Modifier.width(230.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                // Picker de metros (0 a 5)
//                AndroidView(
//                    factory = { context ->
//                        NumberPicker(context).apply {
//                            minValue = 0
//                            maxValue = 5
//                            displayedValues = Array(6) { "$it m" }
//                            value = selectedMeters
//                            setOnValueChangedListener { _, _, newValue ->
//                                selectedMeters = newValue
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .width(80.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                )
//
//                // Picker de decímetros (0 a 90 en pasos de 10)
//                AndroidView(
//                    factory = { context ->
//                        NumberPicker(context).apply {
//                            minValue = 0
//                            maxValue = 9
//                            displayedValues = Array(10) { "${it * 10} cm" }
//                            value = selectedDecimeters / 10
//                            setOnValueChangedListener { _, _, newValue ->
//                                selectedDecimeters = newValue * 10
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .width(80.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                )
//            }
//        },
//        confirmButton = {
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Aquí agregas el ícono de warning (puedes usar cualquier ícono o un Text)
//                Icon(
//                    imageVector = Icons.Default.Warning, // Usa el ícono que prefieras
//                    contentDescription = "Warning",
//                    tint = Color.Yellow, // Puedes cambiar el color
//                    modifier = Modifier.padding(end = 8.dp) // Ajusta el espacio
//                )
//
//                Text(
//                    text = "Al confirmar el robot iniciara la accion",
//                    color = Color.White,
//                )
//
//                val enable = actualDistance != 0F
//                TextButton(
//                    onClick = {
//                        onConfirm(actualDistance);
//                        onDismiss()
//                    },
//                    enabled = enable
//                ) {
//                    Text(
//                        text = stringResource(R.string.action_confirm),
//                        color = if (enable) Color.White else Color.Gray,
//                    )
//                }
//            }
//        }
//    )
//}


@Composable
fun DistancePickerDialog(
    @StringRes directionTitle: Int,
    initialDistance: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMeters = initialDistance.toInt() // Parte entera (metros)
    val initialDecimeters = ((initialDistance - initialMeters) * 10).toInt() // Parte decimal convertida a decímetros

    var selectedMeters by remember { mutableIntStateOf(initialMeters) }
    var selectedDecimeters by remember { mutableIntStateOf(initialDecimeters) }
    var actualDistance by remember { mutableFloatStateOf(0F) }

    LaunchedEffect(selectedMeters, selectedDecimeters) {
        actualDistance = selectedMeters.toFloat() + selectedDecimeters.toFloat().div(10)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        ) {
            Text(
                text = stringResource(R.string.dialog_action_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val contentPickerColor = MaterialTheme.colorScheme.onSurface.toArgb()
                // Picker de metros (0 a 5)
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 5
                            textColor = contentPickerColor
                            displayedValues = Array(6) { "$it m" }
                            value = selectedMeters
                            setOnValueChangedListener { _, _, newValue ->
                                selectedMeters = newValue
                            }
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                // Picker de decímetros (0 a 90 en pasos de 10)
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 9
                            textColor = contentPickerColor
                            displayedValues = Array(10) { "${it * 10} cm" }
                            value = selectedDecimeters
                            setOnValueChangedListener { _, _, newValue ->
                                selectedDecimeters = newValue
                            }
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Yellow
                )

                Text(
                    modifier = Modifier.weight(1F),
                    text = stringResource(R.string.dialog_warning_distance),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                val enable = actualDistance != 0F

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        onConfirm(actualDistance)
                        onDismiss()
                    },
                    enabled = enable
                ) {
                    Text(
                        text = stringResource(directionTitle),
                        color = if (enable) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
@CustomPreviewComponent
fun DistanceDialogPreview() {

    MyAppTheme {
        Column {
//        DistanceDialog(
//            10, {}, {}
//        )
//
//        StepperDistanceDialog(
//            10, {}, {}
//        )
//
//        SwipeNumberDialog(
//            initialDistance = 10,
//            {},
//            {}
//        )

            DistancePickerDialog(
                directionTitle = R.string.title_forward,
                initialDistance = 1.3F,
                onDismiss = {},
                onConfirm = { meters -> },
            )
        }
    }
}