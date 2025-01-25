package com.example.hoverrobot.data.utils

import com.example.hoverrobot.R

object ImagebuttonsMappers {

    private val signalStrengthIcons = mapOf(
        0 to R.drawable.network_wifi_0,
        1 to R.drawable.network_wifi_1,
        2 to R.drawable.network_wifi_2,
        3 to R.drawable.network_wifi_3,
        4 to R.drawable.network_wifi_4
    )

    fun strengthIconMapper(strength: Int): Int {
        return signalStrengthIcons[strength] ?: R.drawable.network_wifi_0
    }
}