package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Bookmark: ImageVector
    get() {
        _Bookmark?.let { return it }
        _Bookmark = ImageVector.Builder(
            name = "Bookmark",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.167f, 5.833f)
                verticalLineTo(20.985f)
                lineTo(11.893f, 18.176f)
                curveTo(13.14f, 17.235f, 14.86f, 17.235f, 16.107f, 18.176f)
                lineTo(19.833f, 20.985f)
                verticalLineTo(5.833f)
                horizontalLineTo(8.167f)
                close()
                moveTo(7f, 3.5f)
                curveTo(6.356f, 3.5f, 5.833f, 4.022f, 5.833f, 4.667f)
                verticalLineTo(23.326f)
                curveTo(5.833f, 24.289f, 6.934f, 24.837f, 7.702f, 24.257f)
                lineTo(13.298f, 20.039f)
                curveTo(13.713f, 19.725f, 14.286f, 19.725f, 14.702f, 20.039f)
                lineTo(20.298f, 24.257f)
                curveTo(21.066f, 24.837f, 22.167f, 24.289f, 22.167f, 23.326f)
                verticalLineTo(4.667f)
                curveTo(22.167f, 4.022f, 21.644f, 3.5f, 21f, 3.5f)
                horizontalLineTo(7f)
                close()
            }
        }.build()

        return checkNotNull(_Bookmark) { "_Bookmark must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Bookmark: ImageVector? = null

val BookmarkFilled: ImageVector
    get() {
        _BookmarkFilled?.let { return it }
        _BookmarkFilled = ImageVector.Builder(
            name = "BookmarkFilled",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 3.5f)
                curveTo(6.356f, 3.5f, 5.833f, 4.022f, 5.833f, 4.667f)
                verticalLineTo(23.326f)
                curveTo(5.833f, 24.289f, 6.934f, 24.837f, 7.702f, 24.257f)
                lineTo(13.298f, 20.039f)
                curveTo(13.713f, 19.725f, 14.286f, 19.725f, 14.702f, 20.039f)
                lineTo(20.298f, 24.257f)
                curveTo(21.066f, 24.837f, 22.167f, 24.289f, 22.167f, 23.326f)
                verticalLineTo(4.667f)
                curveTo(22.167f, 4.022f, 21.644f, 3.5f, 21f, 3.5f)
                horizontalLineTo(7f)
                close()
            }
        }.build()

        return checkNotNull(_BookmarkFilled) { "_BookmarkFilled must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _BookmarkFilled: ImageVector? = null

val Check: ImageVector
    get() {
        _Check?.let { return it }
        _Check = ImageVector.Builder(
            name = "Check",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(19.669f, 6.257f)
                curveTo(20.08f, 6.626f, 20.113f, 7.258f, 19.743f, 7.669f)
                lineTo(10.743f, 17.669f)
                curveTo(10.56f, 17.873f, 10.3f, 17.993f, 10.026f, 18f)
                curveTo(9.752f, 18.007f, 9.487f, 17.901f, 9.293f, 17.707f)
                lineTo(4.293f, 12.707f)
                curveTo(3.902f, 12.317f, 3.902f, 11.683f, 4.293f, 11.293f)
                curveTo(4.683f, 10.902f, 5.317f, 10.902f, 5.707f, 11.293f)
                lineTo(9.962f, 15.548f)
                lineTo(18.257f, 6.331f)
                curveTo(18.626f, 5.921f, 19.258f, 5.887f, 19.669f, 6.257f)
                close()
            }
        }.build()

        return checkNotNull(_Check) { "_Check must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Check: ImageVector? = null

val Ellipsis: ImageVector
    get() {
        _Ellipsis?.let { return it }
        _Ellipsis = ImageVector.Builder(
            name = "Ellipsis",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 11.667f)
                curveTo(8.288f, 11.667f, 9.334f, 12.711f, 9.334f, 14f)
                curveTo(9.334f, 15.288f, 8.288f, 16.334f, 7f, 16.334f)
                curveTo(5.711f, 16.333f, 4.667f, 15.288f, 4.667f, 14f)
                curveTo(4.667f, 12.711f, 5.711f, 11.667f, 7f, 11.667f)
                close()
                moveTo(14f, 11.667f)
                curveTo(15.288f, 11.667f, 16.334f, 12.711f, 16.334f, 14f)
                curveTo(16.334f, 15.288f, 15.288f, 16.334f, 14f, 16.334f)
                curveTo(12.711f, 16.333f, 11.667f, 15.288f, 11.667f, 14f)
                curveTo(11.667f, 12.711f, 12.711f, 11.667f, 14f, 11.667f)
                close()
                moveTo(21f, 11.667f)
                curveTo(22.288f, 11.667f, 23.334f, 12.711f, 23.334f, 14f)
                curveTo(23.334f, 15.288f, 22.288f, 16.334f, 21f, 16.334f)
                curveTo(19.711f, 16.333f, 18.667f, 15.288f, 18.667f, 14f)
                curveTo(18.667f, 12.711f, 19.711f, 11.667f, 21f, 11.667f)
                close()
            }
        }.build()

        return checkNotNull(_Ellipsis) { "_Ellipsis must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Ellipsis: ImageVector? = null

val Share: ImageVector
    get() {
        _Share?.let { return it }
        _Share = ImageVector.Builder(
            name = "Share",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(14.925f, 6.758f)
                curveTo(15.381f, 6.303f, 16.119f, 6.303f, 16.575f, 6.758f)
                lineTo(22.992f, 13.175f)
                curveTo(23.447f, 13.631f, 23.447f, 14.369f, 22.992f, 14.825f)
                lineTo(16.575f, 21.242f)
                curveTo(16.119f, 21.697f, 15.381f, 21.697f, 14.925f, 21.242f)
                curveTo(14.469f, 20.786f, 14.469f, 20.047f, 14.925f, 19.592f)
                lineTo(19.35f, 15.167f)
                horizontalLineTo(10.5f)
                curveTo(8.567f, 15.167f, 7f, 16.734f, 7f, 18.667f)
                verticalLineTo(20.417f)
                curveTo(7f, 21.061f, 6.478f, 21.583f, 5.833f, 21.583f)
                curveTo(5.189f, 21.583f, 4.667f, 21.061f, 4.667f, 20.417f)
                verticalLineTo(18.667f)
                curveTo(4.667f, 15.445f, 7.278f, 12.833f, 10.5f, 12.833f)
                horizontalLineTo(19.35f)
                lineTo(14.925f, 8.408f)
                curveTo(14.469f, 7.953f, 14.469f, 7.214f, 14.925f, 6.758f)
                close()
            }
        }.build()

        return checkNotNull(_Share) { "_Share must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null

val Close: ImageVector
    get() {
        _Close?.let { return it }
        _Close = ImageVector.Builder(
            name = "Close",
            defaultWidth = 45.dp,
            defaultHeight = 45.dp,
            viewportWidth = 45f,
            viewportHeight = 45f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(29.495f, 15.352f)
                curveTo(29.983f, 15.84f, 29.983f, 16.631f, 29.495f, 17.119f)
                lineTo(24.191f, 22.423f)
                lineTo(29.495f, 27.727f)
                curveTo(29.983f, 28.215f, 29.983f, 29.007f, 29.495f, 29.495f)
                curveTo(29.007f, 29.983f, 28.215f, 29.983f, 27.727f, 29.495f)
                lineTo(22.423f, 24.191f)
                lineTo(17.12f, 29.494f)
                curveTo(16.632f, 29.982f, 15.841f, 29.982f, 15.353f, 29.494f)
                curveTo(14.865f, 29.006f, 14.865f, 28.214f, 15.353f, 27.726f)
                lineTo(20.655f, 22.423f)
                lineTo(15.353f, 17.121f)
                curveTo(14.864f, 16.633f, 14.865f, 15.841f, 15.353f, 15.353f)
                curveTo(15.841f, 14.865f, 16.632f, 14.865f, 17.12f, 15.353f)
                lineTo(22.423f, 20.656f)
                lineTo(27.727f, 15.352f)
                curveTo(28.215f, 14.863f, 29.007f, 14.864f, 29.495f, 15.352f)
                close()
            }
        }.build()

        return checkNotNull(_Close) { "_Close must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Close: ImageVector? = null

