package com.app.hoverrobot.data.models.pointCloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PointCloudMetadata(
    val type: String,
    val sequence: Long,
    val stamp: PointCloudStamp,
    @SerialName("frame_id")
    val frameId: String,
    val height: Int,
    val width: Int,
    val fields: List<PointCloudField>,
    @SerialName("is_bigendian")
    val isBigEndian: Boolean,
    @SerialName("point_step")
    val pointStep: Int,
    @SerialName("row_step")
    val rowStep: Int,
    @SerialName("is_dense")
    val isDense: Boolean,
    @SerialName("data_size")
    val dataSize: Int
)

@Serializable
data class PointCloudStamp(
    val sec: Long,
    val nanosec: Long
)

@Serializable
data class PointCloudField(
    val name: String,
    val offset: Int,
    val datatype: Int,
    val count: Int
)