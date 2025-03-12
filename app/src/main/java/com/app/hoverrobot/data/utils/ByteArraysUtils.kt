package com.app.hoverrobot.data.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toByteBuffer(): ByteBuffer {
    val byteBuffer = ByteBuffer.wrap(this)//, 0, bytesRead)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    return byteBuffer
}
