package com.example.hoverrobot.data.utils

import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Locale

object ToolBox {

    val ioScope = CoroutineScope(Dispatchers.IO)

    fun changeStrokeColor(view: View, color: Int, width: Int){

        // Cambio el stroke del drawable de fondo del boton, en lugar del background
        val drawable = view.background as GradientDrawable
        drawable.mutate()
        drawable.setStroke(width, color)
    }

    fun Float.toPercentLevel(): Int {
        return if (this == 0f) 0
        else (((this/10) - MIN_VOLTAGE_PER_CELL_BATTERY) * 100 / (MAX_VOLTAGE_PER_CELL_BATTERY-MIN_VOLTAGE_PER_CELL_BATTERY)).toInt()
    }

    fun Int.toIpString(): String? {
        return String.format(
            Locale.getDefault(),
            "%d.%d.%d.%d", (this and 0xff), (this shr 8 and 0xff), (this shr 16 and 0xff),
            (this shr 24 and 0xff)
        ).takeIf { this != 0 }
    }
}

const val MIN_VOLTAGE_PER_CELL_BATTERY = 3.7f
const val MAX_VOLTAGE_PER_CELL_BATTERY = 4.2f