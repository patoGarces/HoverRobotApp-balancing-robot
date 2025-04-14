package com.app.hoverrobot.ui.screens.navigationScreen.compose

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.hoverrobot.R
import com.app.hoverrobot.ui.composeUtils.CustomPreview
import com.app.hoverrobot.ui.theme.MyAppTheme
import com.redinput.compassview.CompassView

@Composable
fun CompassComposable(
    actualDegress: State<Int>,
    onDragCompass: (Float) -> Unit
) {
    var enableSetNewDegress by remember { mutableStateOf(true) }

    val contentColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val backgroundColor = Color.Transparent.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        factory = { ctx ->
            CompassView(ctx, null).apply {
                setBackgroundColor(backgroundColor)
                setShowMarker(true)
                setMarkerColor(primaryColor)
                setLineColor(contentColor)
                setTextColor(contentColor)
                setTextSize(28)
                setRangeDegrees(50F)
                degrees = actualDegress.value.toFloat()

                // Acceso al mListener por reflection
                val field = CompassView::class.java.getDeclaredField("mListener")
                field.isAccessible = true
                field.set(this, object : CompassView.OnCompassDragListener {
                    override fun onCompassDragListener(degrees: Float) {
                        onDragCompass(degrees)
                    }
                })

                // Detectar Touch sin interferir con el listener de CompassView
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> enableSetNewDegress = false
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> enableSetNewDegress = true
                    }
                    performClick() // Mantiene la accesibilidad
                    false // No bloquea otros eventos táctiles
                }
            }
        },
        update = { compassView ->
            if (enableSetNewDegress) {
                compassView.setDegrees(actualDegress.value.toFloat())
            }
        }
    )
}

@Composable
@CustomPreview
private fun CompassComposablePreview() {
    val dummySetDegress = remember { mutableIntStateOf(0) }
    MyAppTheme {
        CompassComposable(dummySetDegress) { }
    }
}