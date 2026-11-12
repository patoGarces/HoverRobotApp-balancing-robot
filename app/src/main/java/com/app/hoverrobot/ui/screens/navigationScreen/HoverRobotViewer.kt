package com.app.hoverrobot.ui.screens.navigationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.filament.LightManager
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader


@Composable
fun HoverRobot3DViewer(
    modifier: Modifier = Modifier,
    pointCloud: List<Position>,
    robotPosition: Position,
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

    val pointMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF19E6FF))
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
                position = robotPosition,
                rotation = Rotation(
                    x = 90f,
                    y = 0f,
                    z = 0f
                ),
                scale = Scale(1f)
            )
        }

        // Mundo: único elemento que rota con el yaw.
        Node(
            position = Position(0f, 0f, 0f),
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

            // PointCloud en coordenadas del mundo, con origen en el robot.
            pointCloud.forEach { point ->
                SphereNode(
                    radius = 0.015f,
                    position = point,
                    materialInstance = pointMaterial
                )
            }
        }

        LightNode(
            type = LightManager.Type.SUN
        )
    }
}



