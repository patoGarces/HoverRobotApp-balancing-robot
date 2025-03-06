package com.example.hoverrobot.data.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatMillisToDate(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return sdf.format(date)
}
