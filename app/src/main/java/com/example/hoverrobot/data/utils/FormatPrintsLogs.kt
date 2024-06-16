package com.example.hoverrobot.data.utils

object FormatPrintsLogs {
    fun ByteArray.toHex(): String {
        return joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}