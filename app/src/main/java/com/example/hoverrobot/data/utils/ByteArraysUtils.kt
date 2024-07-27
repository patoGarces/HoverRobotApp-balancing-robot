package com.example.hoverrobot.data.utils

import com.example.hoverrobot.data.utils.ByteArraysUtils.u16toInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ByteArraysUtils {

    fun ByteArray.toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.wrap(this)//, 0, bytesRead)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer
    }

    fun ByteArray.u16toInt(): Int {
        return (this[0].toInt() shl 24) or
                (this[1].toInt() shl 16) or
                (this[2].toInt() shl 8) or
                (this[3].toInt())
    }

    fun ByteArray.u32toInt(): Int {
        return (this[0].toInt() shl 24) or
                (this[1].toInt() shl 16) or
                (this[2].toInt() shl 8) or
                (this[3].toInt())
    }

    fun ByteBuffer.get2Bytes(): Int {
        val byte0 = this.get(0).toInt() and 0xFF
        val byte1 = this.get(1).toInt() and 0xFF
        return ((byte1 shl 8) or byte0)        // shl es analogo a '<<' en C
    }
}