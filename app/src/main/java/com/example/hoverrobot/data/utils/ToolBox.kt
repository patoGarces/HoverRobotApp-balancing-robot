package com.example.hoverrobot.data.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ToolBox {

    companion object{
        val ioScope = CoroutineScope(Dispatchers.IO)

        fun changeStrokeColor(view: View, color: Int, width: Int){

            // Cambio el stroke del drawable de fondo del boton, en lugar del background
            val drawable = view.background as GradientDrawable
            drawable.mutate()
            drawable.setStroke(width, color)
        }

        fun Float.toPercentLevel(): Int {
            return (((this/10) - MIN_VOLTAGE_PER_CELL_BATTERY) * 100 / (MAX_VOLTAGE_PER_CELL_BATTERY-MIN_VOLTAGE_PER_CELL_BATTERY)).toInt()
        }
    }
}

const val MIN_VOLTAGE_PER_CELL_BATTERY = 3.7f
const val MAX_VOLTAGE_PER_CELL_BATTERY = 4.2f