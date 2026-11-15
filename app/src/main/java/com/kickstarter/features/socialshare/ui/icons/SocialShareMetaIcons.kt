package com.kickstarter.features.socialshare.ui.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Feature component for SocialShareInstagram in socialshare module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
val SocialShareInstagram: ImageVector
    get() {
        _SocialShareInstagram?.let { return it }
        _SocialShareInstagram = ImageVector.Builder(
            name = "SocialShareInstagram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
            }
            path(
                fill = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDF497),
                        Color(0xFFFD5949),
                        Color(0xFFD6249F),
                        Color(0xFF285AE1)
                    ),
                    start = Offset(0f, 24f),
                    end = Offset(24f, 0f)
                ),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2.162f)
                curveTo(15.204f, 2.162f, 15.584f, 2.174f, 16.849f, 2.232f)
                curveTo(18.019f, 2.285f, 18.655f, 2.48f, 19.077f, 2.645f)
                curveTo(19.638f, 2.863f, 20.037f, 3.122f, 20.457f, 3.542f)
                curveTo(20.877f, 3.962f, 21.137f, 4.362f, 21.355f, 4.922f)
                curveTo(21.519f, 5.345f, 21.715f, 5.98f, 21.768f, 7.15f)
                curveTo(21.826f, 8.416f, 21.838f, 8.795f, 21.838f, 12f)
                curveTo(21.838f, 15.204f, 21.826f, 15.583f, 21.768f, 16.849f)
                curveTo(21.715f, 18.019f, 21.519f, 18.654f, 21.355f, 19.077f)
                curveTo(21.137f, 19.637f, 20.877f, 20.037f, 20.457f, 20.457f)
                curveTo(20.037f, 20.877f, 19.638f, 21.137f, 19.077f, 21.354f)
                curveTo(18.655f, 21.519f, 18.019f, 21.714f, 16.849f, 21.767f)
                curveTo(15.584f, 21.825f, 15.204f, 21.837f, 12f, 21.837f)
                curveTo(8.796f, 21.837f, 8.416f, 21.825f, 7.151f, 21.767f)
                curveTo(5.981f, 21.714f, 5.345f, 21.519f, 4.923f, 21.354f)
                curveTo(4.362f, 21.136f, 3.963f, 20.877f, 3.543f, 20.457f)
                curveTo(3.123f, 20.037f, 2.863f, 19.637f, 2.645f, 19.077f)
                curveTo(2.481f, 18.654f, 2.286f, 18.019f, 2.232f, 16.849f)
                curveTo(2.174f, 15.583f, 2.162f, 15.204f, 2.162f, 12f)
                curveTo(2.162f, 8.795f, 2.174f, 8.416f, 2.232f, 7.15f)
                curveTo(2.286f, 5.98f, 2.481f, 5.345f, 2.645f, 4.922f)
                curveTo(2.863f, 4.362f, 3.123f, 3.962f, 3.543f, 3.542f)
                curveTo(3.963f, 3.122f, 4.362f, 2.862f, 4.923f, 2.645f)
                curveTo(5.345f, 2.48f, 5.981f, 2.285f, 7.151f, 2.232f)
                curveTo(8.416f, 2.174f, 8.796f, 2.162f, 12f, 2.162f)
                close()
                moveTo(12f, 0f)
                curveTo(8.741f, 0f, 8.332f, 0.014f, 7.052f, 0.072f)
                curveTo(5.775f, 0.13f, 4.903f, 0.333f, 4.14f, 0.63f)
                curveTo(3.35f, 0.937f, 2.681f, 1.347f, 2.014f, 2.014f)
                curveTo(1.347f, 2.681f, 0.936f, 3.35f, 0.63f, 4.14f)
                curveTo(0.333f, 4.903f, 0.13f, 5.775f, 0.072f, 7.052f)
                curveTo(0.014f, 8.332f, 0f, 8.741f, 0f, 12f)
                curveTo(0f, 15.259f, 0.014f, 15.668f, 0.072f, 16.948f)
                curveTo(0.13f, 18.225f, 0.333f, 19.097f, 0.63f, 19.86f)
                curveTo(0.937f, 20.65f, 1.347f, 21.319f, 2.014f, 21.986f)
                curveTo(2.681f, 22.653f, 3.351f, 23.063f, 4.14f, 23.37f)
                curveTo(4.903f, 23.667f, 5.775f, 23.87f, 7.052f, 23.928f)
                curveTo(8.332f, 23.986f, 8.741f, 24f, 12f, 24f)
                curveTo(15.259f, 24f, 15.668f, 23.986f, 16.948f, 23.928f)
                curveTo(18.225f, 23.87f, 19.097f, 23.667f, 19.86f, 23.37f)
                curveTo(20.65f, 23.063f, 21.319f, 22.653f, 21.986f, 21.986f)
                curveTo(22.653f, 21.319f, 23.063f, 20.649f, 23.37f, 19.86f)
                curveTo(23.667f, 19.097f, 23.87f, 18.225f, 23.928f, 16.948f)
                curveTo(23.986f, 15.668f, 24f, 15.259f, 24f, 12f)
                curveTo(24f, 8.741f, 23.986f, 8.332f, 23.928f, 7.052f)
                curveTo(23.87f, 5.775f, 23.667f, 4.903f, 23.37f, 4.14f)
                curveTo(23.063f, 3.35f, 22.653f, 2.681f, 21.986f, 2.014f)
                curveTo(21.319f, 1.347f, 20.649f, 0.937f, 19.86f, 0.63f)
                curveTo(19.097f, 0.333f, 18.225f, 0.13f, 16.948f, 0.072f)
                curveTo(15.668f, 0.014f, 15.259f, 0f, 12f, 0f)
                close()
                moveTo(12f, 5.838f)
                curveTo(8.597f, 5.838f, 5.838f, 8.597f, 5.838f, 12f)
                curveTo(5.838f, 15.403f, 8.597f, 18.162f, 12f, 18.162f)
                curveTo(15.403f, 18.162f, 18.162f, 15.403f, 18.162f, 12f)
                curveTo(18.162f, 8.597f, 15.403f, 5.838f, 12f, 5.838f)
                close()
                moveTo(12f, 16f)
                curveTo(9.791f, 16f, 8f, 14.21f, 8f, 12f)
                curveTo(8f, 9.79f, 9.791f, 8f, 12f, 8f)
                curveTo(14.209f, 8f, 16f, 9.79f, 16f, 12f)
                curveTo(16f, 14.21f, 14.209f, 16f, 12f, 16f)
                close()
                moveTo(18.406f, 4.155f)
                curveTo(17.61f, 4.155f, 16.965f, 4.799f, 16.965f, 5.595f)
                curveTo(16.965f, 6.39f, 17.61f, 7.035f, 18.406f, 7.035f)
                curveTo(19.201f, 7.035f, 19.846f, 6.39f, 19.846f, 5.595f)
                curveTo(19.846f, 4.799f, 19.201f, 4.155f, 18.406f, 4.155f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareInstagram) { "_SocialShareInstagram must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareInstagram: ImageVector? = null

val SocialShareFacebook: ImageVector
    get() {
        _SocialShareFacebook?.let { return it }
        _SocialShareFacebook = ImageVector.Builder(
            name = "SocialShareFacebook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
            }
            group(
                clipPathData = PathData {
                    moveTo(-4.8f, -4.8f)
                    horizontalLineTo(28.8f)
                    verticalLineTo(28.8f)
                    horizontalLineTo(-4.8f)
                    verticalLineTo(-4.8f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF0866FF))) {
                    moveTo(24f, 12f)
                    curveTo(24f, 5.373f, 18.627f, -0f, 12f, -0f)
                    curveTo(5.373f, -0f, 0f, 5.373f, 0f, 12f)
                    curveTo(0f, 17.628f, 3.874f, 22.35f, 9.101f, 23.647f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(6.627f)
                    verticalLineTo(12f)
                    horizontalLineTo(9.101f)
                    verticalLineTo(10.42f)
                    curveTo(9.101f, 6.335f, 10.95f, 4.442f, 14.959f, 4.442f)
                    curveTo(15.72f, 4.442f, 17.031f, 4.591f, 17.568f, 4.74f)
                    verticalLineTo(8.065f)
                    curveTo(17.285f, 8.035f, 16.793f, 8.02f, 16.182f, 8.02f)
                    curveTo(14.214f, 8.02f, 13.454f, 8.765f, 13.454f, 10.703f)
                    verticalLineTo(12f)
                    horizontalLineTo(17.373f)
                    lineTo(16.7f, 15.667f)
                    horizontalLineTo(13.454f)
                    verticalLineTo(23.912f)
                    curveTo(19.396f, 23.194f, 24f, 18.135f, 24f, 12f)
                    close()
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(16.7f, 15.667f)
                    lineTo(17.373f, 12f)
                    horizontalLineTo(13.454f)
                    verticalLineTo(10.703f)
                    curveTo(13.454f, 8.765f, 14.214f, 8.02f, 16.182f, 8.02f)
                    curveTo(16.793f, 8.02f, 17.285f, 8.035f, 17.568f, 8.065f)
                    verticalLineTo(4.74f)
                    curveTo(17.031f, 4.591f, 15.719f, 4.442f, 14.959f, 4.442f)
                    curveTo(10.949f, 4.442f, 9.101f, 6.335f, 9.101f, 10.42f)
                    verticalLineTo(12f)
                    horizontalLineTo(6.626f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(9.101f)
                    verticalLineTo(23.647f)
                    curveTo(10.029f, 23.877f, 11f, 24f, 12f, 24f)
                    curveTo(12.492f, 24f, 12.977f, 23.97f, 13.454f, 23.912f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(16.7f)
                    close()
                }
            }
        }.build()

        return checkNotNull(_SocialShareFacebook) { "_SocialShareFacebook must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareFacebook: ImageVector? = null

