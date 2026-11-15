package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * UI component rendering WhatshotPreview.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Preview
@Composable
private fun WhatshotPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Whatshot, contentDescription = null)
    }
}

@Preview
@Composable
private fun ProjectWeLovePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = ProjectWeLove, contentDescription = null)
    }
}

@Preview
@Composable
private fun ClockPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Clock, contentDescription = null)
    }
}

val Whatshot: ImageVector
    get() {
        val existing = _Whatshot
        if (existing != null) {
            return existing
        }
        val built = ImageVector.Builder(
            name = "Whatshot",
            defaultWidth = 12.dp,
            defaultHeight = 15.dp,
            viewportWidth = 12f,
            viewportHeight = 15f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6.074f, 0.083f)
                curveTo(5.432f, 0.546f, 4.908f, 1.155f, 4.548f, 1.86f)
                curveTo(4.187f, 2.565f, 3.999f, 3.346f, 4f, 4.138f)
                verticalLineTo(4.17f)
                curveTo(4.004f, 4.77f, 4.114f, 5.346f, 4.311f, 5.879f)
                curveTo(4.471f, 6.307f, 4.107f, 6.789f, 3.701f, 6.579f)
                curveTo(2.946f, 6.187f, 2.304f, 5.61f, 1.833f, 4.902f)
                curveTo(1.631f, 4.598f, 1.185f, 4.539f, 0.985f, 4.844f)
                curveTo(0.326f, 5.848f, -0.017f, 7.027f, 0.001f, 8.228f)
                curveTo(0.019f, 9.429f, 0.397f, 10.597f, 1.086f, 11.581f)
                curveTo(1.776f, 12.565f, 2.745f, 13.319f, 3.867f, 13.746f)
                curveTo(4.99f, 14.173f, 6.215f, 14.253f, 7.384f, 13.976f)
                curveTo(8.553f, 13.699f, 9.612f, 13.077f, 10.424f, 12.192f)
                curveTo(11.235f, 11.306f, 11.762f, 10.197f, 11.936f, 9.008f)
                curveTo(12.111f, 7.82f, 11.924f, 6.606f, 11.401f, 5.525f)
                curveTo(10.878f, 4.444f, 10.042f, 3.544f, 9.002f, 2.943f)
                lineTo(8.998f, 2.936f)
                curveTo(7.994f, 2.358f, 7.223f, 1.447f, 6.818f, 0.362f)
                curveTo(6.702f, 0.052f, 6.341f, -0.109f, 6.074f, 0.083f)
                close()
                moveTo(6.854f, 6.261f)
                curveTo(7.326f, 6.401f, 7.756f, 6.655f, 8.106f, 7f)
                curveTo(8.457f, 7.345f, 8.717f, 7.771f, 8.864f, 8.24f)
                curveTo(9.012f, 8.71f, 9.041f, 9.208f, 8.95f, 9.691f)
                curveTo(8.86f, 10.175f, 8.651f, 10.629f, 8.344f, 11.013f)
                curveTo(8.037f, 11.397f, 7.64f, 11.7f, 7.188f, 11.894f)
                curveTo(6.736f, 12.089f, 6.243f, 12.17f, 5.753f, 12.129f)
                curveTo(5.263f, 12.088f, 4.79f, 11.928f, 4.377f, 11.662f)
                curveTo(3.963f, 11.395f, 3.621f, 11.031f, 3.381f, 10.602f)
                curveTo(3.176f, 10.237f, 3.596f, 9.908f, 4.001f, 10.012f)
                curveTo(4.614f, 10.17f, 5.256f, 10.18f, 5.874f, 10.042f)
                curveTo(6.162f, 9.977f, 6.287f, 9.656f, 6.195f, 9.376f)
                curveTo(6.065f, 8.976f, 5.999f, 8.558f, 6f, 8.137f)
                curveTo(6f, 7.552f, 6.126f, 6.997f, 6.351f, 6.496f)
                curveTo(6.392f, 6.402f, 6.466f, 6.327f, 6.558f, 6.284f)
                curveTo(6.651f, 6.24f, 6.756f, 6.232f, 6.854f, 6.261f)
                close()
            }
        }.build()

        _Whatshot = built
        return built
    }

@Suppress("ObjectPropertyName")
private var _Whatshot: ImageVector? = null

val ProjectWeLove: ImageVector
    get() {
        val existing = _ProjectWeLove
        if (existing != null) {
            return existing
        }
        val built = ImageVector.Builder(
            name = "ProjectWeLove",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(16f)
                    verticalLineToRelative(16f)
                    horizontalLineToRelative(-16f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF05CE78))) {
                    moveTo(5.787f, 1.669f)
                    curveTo(6.792f, 0.003f, 9.208f, 0.003f, 10.213f, 1.669f)
                    curveTo(10.672f, 2.429f, 11.489f, 2.901f, 12.377f, 2.918f)
                    curveTo(14.322f, 2.955f, 15.53f, 5.048f, 14.59f, 6.751f)
                    curveTo(14.16f, 7.528f, 14.16f, 8.472f, 14.59f, 9.249f)
                    curveTo(15.53f, 10.953f, 14.322f, 13.045f, 12.377f, 13.082f)
                    curveTo(11.489f, 13.099f, 10.672f, 13.571f, 10.213f, 14.331f)
                    curveTo(9.208f, 15.997f, 6.792f, 15.997f, 5.787f, 14.331f)
                    curveTo(5.328f, 13.571f, 4.511f, 13.099f, 3.623f, 13.082f)
                    curveTo(1.678f, 13.045f, 0.47f, 10.953f, 1.41f, 9.249f)
                    curveTo(1.84f, 8.472f, 1.84f, 7.528f, 1.41f, 6.751f)
                    curveTo(0.47f, 5.048f, 1.678f, 2.955f, 3.623f, 2.918f)
                    curveTo(4.511f, 2.901f, 5.328f, 2.429f, 5.787f, 1.669f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF024629)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(3.61f, 5.1f)
                    curveTo(4.119f, 4.386f, 4.914f, 4f, 5.896f, 4f)
                    curveTo(6.796f, 4f, 7.492f, 4.315f, 7.971f, 4.645f)
                    curveTo(8.437f, 4.307f, 9.112f, 4f, 10f, 4f)
                    curveTo(10.976f, 4f, 11.781f, 4.38f, 12.314f, 5.068f)
                    curveTo(12.818f, 5.72f, 13f, 6.543f, 13f, 7.322f)
                    curveTo(13f, 8.293f, 12.586f, 9.359f, 11.548f, 10.381f)
                    curveTo(11.526f, 10.402f, 11.503f, 10.422f, 11.48f, 10.441f)
                    lineTo(8.634f, 12.774f)
                    curveTo(8.265f, 13.075f, 7.735f, 13.075f, 7.366f, 12.774f)
                    lineTo(4.52f, 10.441f)
                    curveTo(4.497f, 10.422f, 4.474f, 10.402f, 4.452f, 10.381f)
                    curveTo(3.414f, 9.359f, 3f, 8.293f, 3f, 7.322f)
                    curveTo(3f, 6.577f, 3.141f, 5.759f, 3.61f, 5.1f)
                    close()
                    moveTo(8.863f, 6.539f)
                    curveTo(8.86f, 6.544f, 8.857f, 6.549f, 8.854f, 6.554f)
                    curveTo(8.853f, 6.556f, 8.853f, 6.557f, 8.854f, 6.555f)
                    lineTo(8.857f, 6.549f)
                    lineTo(8.86f, 6.545f)
                    lineTo(8.862f, 6.542f)
                    lineTo(8.863f, 6.54f)
                    lineTo(8.863f, 6.539f)
                    close()
                    moveTo(10.176f, 8.925f)
                    curveTo(10.864f, 8.235f, 11f, 7.674f, 11f, 7.322f)
                    curveTo(11f, 6.821f, 10.88f, 6.482f, 10.733f, 6.293f)
                    curveTo(10.615f, 6.141f, 10.42f, 6f, 10f, 6f)
                    curveTo(9.569f, 6f, 9.282f, 6.154f, 9.098f, 6.3f)
                    curveTo(9.003f, 6.376f, 8.935f, 6.451f, 8.894f, 6.502f)
                    curveTo(8.873f, 6.527f, 8.861f, 6.545f, 8.856f, 6.552f)
                    curveTo(8.855f, 6.553f, 8.855f, 6.554f, 8.854f, 6.554f)
                    curveTo(8.68f, 6.841f, 8.372f, 7.022f, 8.036f, 7.034f)
                    curveTo(7.698f, 7.046f, 7.377f, 6.886f, 7.183f, 6.611f)
                    curveTo(7.182f, 6.609f, 7.18f, 6.608f, 7.179f, 6.605f)
                    curveTo(7.17f, 6.595f, 7.153f, 6.574f, 7.128f, 6.545f)
                    curveTo(7.076f, 6.488f, 6.992f, 6.406f, 6.879f, 6.323f)
                    curveTo(6.656f, 6.158f, 6.333f, 6f, 5.896f, 6f)
                    curveTo(5.482f, 6f, 5.329f, 6.134f, 5.239f, 6.261f)
                    curveTo(5.109f, 6.444f, 5f, 6.786f, 5f, 7.322f)
                    curveTo(5f, 7.674f, 5.136f, 8.235f, 5.824f, 8.925f)
                    lineTo(8f, 10.707f)
                    lineTo(10.176f, 8.925f)
                    close()
                }
            }
        }.build()

        _ProjectWeLove = built
        return built
    }

@Suppress("ObjectPropertyName")
private var _ProjectWeLove: ImageVector? = null

val Clock: ImageVector
    get() {
        val existing = _Clock
        if (existing != null) {
            return existing
        }
        val built = ImageVector.Builder(
            name = "Clock",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(16f)
                    verticalLineToRelative(16f)
                    horizontalLineToRelative(-16f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color.White)) {
                    moveTo(8f, 0f)
                    curveTo(12.418f, 0f, 16f, 3.582f, 16f, 8f)
                    curveTo(16f, 12.418f, 12.418f, 16f, 8f, 16f)
                    curveTo(3.582f, 16f, 0f, 12.418f, 0f, 8f)
                    curveTo(0f, 3.582f, 3.582f, 0f, 8f, 0f)
                    close()
                    moveTo(8f, 2f)
                    curveTo(4.686f, 2f, 2f, 4.686f, 2f, 8f)
                    curveTo(2f, 11.314f, 4.686f, 14f, 8f, 14f)
                    curveTo(11.314f, 14f, 14f, 11.314f, 14f, 8f)
                    curveTo(14f, 4.686f, 11.314f, 2f, 8f, 2f)
                    close()
                    moveTo(8f, 3f)
                    curveTo(8.552f, 3f, 9f, 3.448f, 9f, 4f)
                    verticalLineTo(8f)
                    horizontalLineTo(11f)
                    curveTo(11.552f, 8f, 12f, 8.448f, 12f, 9f)
                    curveTo(12f, 9.552f, 11.552f, 10f, 11f, 10f)
                    horizontalLineTo(8f)
                    curveTo(7.448f, 10f, 7f, 9.552f, 7f, 9f)
                    verticalLineTo(4f)
                    curveTo(7f, 3.448f, 7.448f, 3f, 8f, 3f)
                    close()
                }
            }
        }.build()

        _Clock = built
        return built
    }

@Suppress("ObjectPropertyName")
private var _Clock: ImageVector? = null
