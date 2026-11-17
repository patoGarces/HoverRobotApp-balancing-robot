package com.app.hoverrobot.data.models.pointCloud

import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.ByteOrder


data class PointCloudData(
    val metadata: PointCloudMetadata,
    val data: ByteArray
)

data class PointCloudItem(
    val positions: FloatArray = FloatArray(0),
    val colors: FloatArray = FloatArray(0),
    val pointCount: Int = 0
)

fun PointCloudData.toPointCloudItem(): PointCloudItem {
    val buffer = ByteBuffer.wrap(data).order(
        if (metadata.isBigEndian) ByteOrder.BIG_ENDIAN
        else ByteOrder.LITTLE_ENDIAN
    )

    val positions = FloatArray(metadata.width * metadata.height * 3)
    val colors = FloatArray(metadata.width * metadata.height * 4)
    var pointIndex = 0

    for (row in 0 until metadata.height) {
        for (col in 0 until metadata.width) {
            val offset = row * metadata.rowStep + col * metadata.pointStep

            val x = buffer.getFloat(offset)
            val y = buffer.getFloat(offset + 4)
            val z = buffer.getFloat(offset + 8)

            if (!x.isFinite() || !y.isFinite() || !z.isFinite()) continue

            val positionOffset = pointIndex * 3
            positions[positionOffset] = x
            positions[positionOffset + 1] = y
            positions[positionOffset + 2] = z

            val rgb = buffer.getFloat(offset + 16).toRawBits()

            val colorOffset = pointIndex * 4
            colors[colorOffset] = ((rgb shr 16) and 0xFF) / 255f
            colors[colorOffset + 1] = ((rgb shr 8) and 0xFF) / 255f
            colors[colorOffset + 2] = (rgb and 0xFF) / 255f
            colors[colorOffset + 3] = 1f

            pointIndex++
        }
    }

    return PointCloudItem(
        positions = positions.copyOf(pointIndex * 3),
        colors = colors.copyOf(pointIndex * 4),
        pointCount = pointIndex
    )
}

fun PointCloudData.toPointCloudItem2(): PointCloudItem {
    val buffer = ByteBuffer.wrap(data).order(
        if (metadata.isBigEndian) ByteOrder.BIG_ENDIAN
        else ByteOrder.LITTLE_ENDIAN
    )

    val positions = FloatArray(metadata.width * metadata.height * 3)
    var pointIndex = 0

    for (row in 0 until metadata.height) {
        for (col in 0 until metadata.width) {
            val offset = row * metadata.rowStep + col * metadata.pointStep

            val x = buffer.getFloat(offset)
            val y = buffer.getFloat(offset + 4)
            val z = buffer.getFloat(offset + 8)

            if (!x.isFinite() || !y.isFinite() || !z.isFinite()) continue

            val positionOffset = pointIndex * 3
            positions[positionOffset] = x
            positions[positionOffset + 1] = y
            positions[positionOffset + 2] = z

            pointIndex++
        }
    }

    return PointCloudItem(
        positions = positions.copyOf(pointIndex * 3),
        pointCount = pointIndex
    )
}