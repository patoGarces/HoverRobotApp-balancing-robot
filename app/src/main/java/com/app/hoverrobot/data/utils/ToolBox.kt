package com.app.hoverrobot.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Locale

object ToolBox {

    val ioScope = CoroutineScope(Dispatchers.IO)

    fun Int.toIpString(): String? {
        return String.format(
            Locale.getDefault(),
            "%d.%d.%d.%d", (this and 0xff), (this shr 8 and 0xff), (this shr 16 and 0xff),
            (this shr 24 and 0xff)
        ).takeIf { this != 0 }
    }
}