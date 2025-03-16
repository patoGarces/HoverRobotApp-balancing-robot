package com.app.hoverrobot.data.models

enum class Aggressiveness(val normalizedFactor: Float) {
    SOFT(0.3F),
    MODERATE(0.6F),
    AGGRESSIVE(1F);
}