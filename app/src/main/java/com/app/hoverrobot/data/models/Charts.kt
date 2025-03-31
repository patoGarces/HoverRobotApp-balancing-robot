package com.app.hoverrobot.data.models

import com.github.mikephil.charting.components.LimitLine

data class ChartLimitsConfig (
    val limitAxis: Float,
    val limitLines: List<LimitLine>? = null
)