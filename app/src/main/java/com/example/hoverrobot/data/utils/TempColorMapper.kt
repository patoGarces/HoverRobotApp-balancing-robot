package com.example.hoverrobot.data.utils

import android.content.Context
import com.example.hoverrobot.R

object TempColorMapper {
    //    fun mapTempToColorInterpolate(temp: Float): Int {
//        // Rango de temperatura
//        val minTemperature = 0.0
//        val maxTemperature = 5.0
//
//        // Rango de colores RGB para la transición de azul a rojo
//        val minColor = intArrayOf(255, 255, 255)  // Azul (RGB)
//        val maxColor = intArrayOf(255, 0, 0)  // Rojo (RGB)
//
//        // Ajustar la temperatura al rango 0-1
//        val normalizedTemperature = (temp - minTemperature) / (maxTemperature - minTemperature)
//
//        // Interpolacion entre colores
//        val interpolatedColor = IntArray(3)
//        for (i in 0 until 3) {
//            interpolatedColor[i] = (minColor[i] + (maxColor[i] - minColor[i]) * normalizedTemperature).toInt()
//        }
//
//        return android.graphics.Color.rgb(interpolatedColor[0], interpolatedColor[1], interpolatedColor[2])
//    }
    fun mapTempToColor(temp: Float): Int {
        return when {
            temp < 10.0 -> R.color.status_blue
            temp < 45.0 -> R.color.white
            temp < 55.0 -> R.color.status_orange
            else -> R.color.status_red
        }
    }
}