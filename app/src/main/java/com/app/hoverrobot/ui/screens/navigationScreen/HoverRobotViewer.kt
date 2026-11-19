package com.app.hoverrobot.ui.screens.navigationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.app.hoverrobot.data.models.pointCloud.PointCloudItem
import com.google.android.filament.LightManager
import com.google.android.filament.RenderableManager
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.SceneView
import io.github.sceneview.geometries.Geometry
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.GeometryNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader


@Composable
fun HoverRobot3DViewer(
    modifier: Modifier = Modifier,
    pointCloud: PointCloudItem,
    offsetRobot: Position,
    robotYawDegrees: Float = 0f,
    robotModelPath: String = "models/hoverrobot.glb"
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)

    val robotModel = rememberModelInstance(
        modelLoader,
        robotModelPath
    )

    // Cámara fija detrás y arriba del robot.
    val cameraNode = rememberCameraNode(engine) {
        position = Position(
            x = -3.5f,
            y = 2.0f,
            z = 0f
        )
        lookAt(Position(0f, 0f, 0f))
    }

    // Materiales.
    val gridMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF38485C))
    }

    val majorGridMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF607080))
    }

    val xAxisMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color.Red)
    }

    val yAxisMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color.Green)
    }

    val zAxisMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color.Blue)
    }

//    val pointMaterial = remember(materialLoader) {
//        materialLoader.createColorInstance(Color(0xFF19E6FF))
//    }

    val context = LocalContext.current

    val pointMaterial = remember(engine, context) {
        val bytes = context.assets
            .open("materials/point_cloud.filamat")
            .use { it.readBytes() }

        val buffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(bytes)
                flip()
            }

        Material.Builder()
            .payload(buffer, bytes.size)
            .build(engine)
            .createInstance()
    }

    // Cuadrícula 10 x 10 m, líneas cada 25 cm.
    val gridLines = remember {
        buildList {
            for (i in -20..20) {
                val offset = i * 0.25f
                val isMajor = i % 4 == 0

                add(
                    Triple(
                        Position(offset, 0f, -5f),
                        Position(offset, 0f, 5f),
                        isMajor
                    )
                )

                add(
                    Triple(
                        Position(-5f, 0f, offset),
                        Position(5f, 0f, offset),
                        isMajor
                    )
                )
            }
        }
    }

    val pointCloudGeometryData = remember(pointCloud) {
        val count = minOf(
            pointCloud.pointCount,
            pointCloud.positions.size / 3
        )

        if (count == 0) {
            listOf(Geometry.Vertex(position = Position())) to listOf(0)
        } else {
            val vertices = ArrayList<Geometry.Vertex>(count)
            val indices = ArrayList<Int>(count)

            for (i in 0 until count) {
                val offset = i * 3
                val colorOffset = i * 4

                vertices.add(
                    Geometry.Vertex(
                        position = Position(
                            pointCloud.positions[offset],
                            -pointCloud.positions[offset + 1],
                            -pointCloud.positions[offset + 2]
                        ),
                        color = Float4(
                            pointCloud.colors[colorOffset],
                            pointCloud.colors[colorOffset + 1],
                            pointCloud.colors[colorOffset + 2],
                            pointCloud.colors[colorOffset + 3]
                        )
                    )
                )

                indices.add(i)
            }

            vertices to indices
        }
    }

    val pointCloudNode = remember(engine, pointMaterial) {
        val initialGeometry = Geometry.Builder(
            RenderableManager.PrimitiveType.POINTS
        )
            .vertices(
                listOf(
                    Geometry.Vertex(
                        position = Position(0.001f, 0.001f, 0.001f)
                    )
                )
            )
            .indices(listOf(0))
            .build(engine)

        GeometryNode(
            engine = engine,
            geometry = initialGeometry,
            materialInstance = pointMaterial,
            builderApply = {
                culling(false)
                castShadows(false)
                receiveShadows(false)
            }
        )
    }

//    val yawRad = Math.toRadians(robotYawDegrees.toDouble())
//    val cosYaw = cos(yawRad).toFloat()
//    val sinYaw = sin(yawRad).toFloat()
//
//    val worldPosition = Position(
//        x = -cosYaw * offsetRobot.x + sinYaw * offsetRobot.z,
//        y = -offsetRobot.y,
//        z = -sinYaw * offsetRobot.x - cosYaw * offsetRobot.z
//    )
    val worldPosition = Position(x = 0f, y = 0f, z = 0f)

    SideEffect {
        val (vertices, indices) = pointCloudGeometryData

        pointCloudNode.isVisible = pointCloud.pointCount > 0

        if (pointCloud.pointCount > 0) {
            pointCloudNode.updateGeometry(
                vertices = vertices,
                indices = listOf(indices)
            )
        }
    }

    DisposableEffect(pointCloudNode) {
        onDispose {
            pointCloudNode.destroy()
        }
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        cameraNode = cameraNode,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
//        cameraManipulator = null,
        autoCenterContent = false
    ) {
        // Robot fijo en el origen.
        robotModel?.let { model ->
            ModelNode(
                modelInstance = model,
                position = offsetRobot,
                rotation = Rotation(
                    x = 90f,
                    y = 0f,
                    z = 0f
                ),
                scale = Scale(1f)
            )
        }

        // PointCloud fijo respecto al robot.
        Node(
            position = Position(0f, 0f, 0f),
            rotation = Rotation(0f, -90f, 0f),
            apply = {
                addChildNode(pointCloudNode)
            }
        )

        // Mundo: se mueve y rota en sentido inverso al robot.
        Node(
            position = worldPosition,
            rotation = Rotation(
                x = 0f,
                y = -robotYawDegrees,
                z = 0f
            )
        ) {
            // Cuadrícula.
            gridLines.forEach { (start, end, isMajor) ->
                LineNode(
                    start,
                    end,
                    if (isMajor) majorGridMaterial else gridMaterial
                )
            }

            // Ejes.
            LineNode(
                Position(-5f, 0.01f, 0f),
                Position(5f, 0.01f, 0f),
                xAxisMaterial
            )

            LineNode(
                Position(0f, 0f, 0f),
                Position(0f, 1.5f, 0f),
                yAxisMaterial
            )

            LineNode(
                Position(0f, 0.01f, -5f),
                Position(0f, 0.01f, 5f),
                zAxisMaterial
            )
        }

        LightNode(
            type = LightManager.Type.SUN
        )
    }
}



