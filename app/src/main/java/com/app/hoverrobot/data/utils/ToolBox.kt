package com.app.hoverrobot.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

object ToolBox {

    val ioScope = CoroutineScope(Dispatchers.IO)

    fun Float.round(decimals: Int = 2): Float {
        val factor = 10.0.pow(decimals.toDouble()).toFloat()
        return (this * factor).roundToInt() / factor
    }

    fun Int.formatAsIp(): String? {
        if (this == 0) return null

        val bytes = listOf(
            this and 0xFF,
            (this shr 8) and 0xFF,
            (this shr 16) and 0xFF,
            (this shr 24) and 0xFF
        )
        return bytes.joinToString(".")
    }
}