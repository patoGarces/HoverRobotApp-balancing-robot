package com.example.hoverrobot

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ToolBox {

    companion object{

        val ioScope = CoroutineScope(Dispatchers.IO)

        fun changeStrokeColor(context : Context, view : View, color : Int, width : Int){

            // Cambio el stroke del drawable de fondo del boton, en lugar del background
            val drawable = view.background as GradientDrawable
            drawable.mutate()
            drawable.setStroke(width, context.getColor(color))
        }
    }
}