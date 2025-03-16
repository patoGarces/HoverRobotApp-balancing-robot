package com.app.hoverrobot.ui.navigationFragment.compose

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.hoverrobot.R
import com.redinput.compassview.CompassView

@Composable
fun CompassComposable(
    actualDegress: State<Int>,
    onDragCompass: (Float) -> Unit
) {
    var enableSetNewDegress by remember { mutableStateOf(true) }

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        factory = { ctx ->
            CompassView(ctx, null).apply {
                setBackgroundColor(ctx.getColor(R.color.transparent))
                setShowMarker(true)
                setMarkerColor(ctx.getColor(R.color.red_80_percent))
                setLineColor(ctx.getColor(R.color.white))
                setTextColor(ctx.getColor(R.color.white))
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

@Preview
@Composable
private fun CompassComposablePreview() {

    val dummySetDegress = remember { mutableIntStateOf(0) }
    CompassComposable(dummySetDegress) { }
}