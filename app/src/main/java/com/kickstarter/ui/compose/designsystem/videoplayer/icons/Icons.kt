package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * UI component rendering RewindPreview.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Preview
@Composable
private fun RewindPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Rewind, contentDescription = null)
    }
}

@Preview
@Composable
private fun PlayPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Play, contentDescription = null)
    }
}

@Preview
@Composable
private fun ForwardPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Forward, contentDescription = null)
    }
}

@Preview
@Composable
private fun BookmarkPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Bookmark, contentDescription = null)
    }
}

@Preview
@Composable
private fun CheckPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Check, contentDescription = null)
    }
}

@Preview
@Composable
private fun EllipsisPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Ellipsis, contentDescription = null)
    }
}

@Preview
@Composable
private fun ReplyPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Share, contentDescription = null)
    }
}

@Preview
@Composable
private fun ClosePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Close, contentDescription = null)
    }
}

@Preview
@Composable
private fun ExpandPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Expand, contentDescription = null)
    }
}

@Preview
@Composable
private fun CollapsePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Collapse, contentDescription = null)
    }
}

@Preview
@Composable
private fun MutePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Mute, contentDescription = null)
    }
}

@Preview
@Composable
private fun VolumeUpPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = VolumeUp, contentDescription = null)
    }
}

val Expand: ImageVector
    get() {
        _Expand?.let { return it }
        _Expand = ImageVector.Builder(
            name = "Expand",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 12f)
                curveTo(19.448f, 12f, 19f, 11.552f, 19f, 11f)
                verticalLineTo(6.414f)
                lineTo(14.707f, 10.707f)
                curveTo(14.317f, 11.098f, 13.683f, 11.098f, 13.293f, 10.707f)
                curveTo(12.902f, 10.317f, 12.902f, 9.683f, 13.293f, 9.293f)
                lineTo(17.586f, 5f)
                lineTo(13f, 5f)
                curveTo(12.448f, 5f, 12f, 4.552f, 12f, 4f)
                curveTo(12f, 3.448f, 12.448f, 3f, 13f, 3f)
                horizontalLineTo(20f)
                curveTo(20.265f, 3f, 20.52f, 3.105f, 20.707f, 3.293f)
                curveTo(20.895f, 3.48f, 21f, 3.735f, 21f, 4f)
                lineTo(21f, 11f)
                curveTo(21f, 11.552f, 20.552f, 12f, 20f, 12f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 12f)
                curveTo(4.552f, 12f, 5f, 12.448f, 5f, 13f)
                verticalLineTo(17.586f)
                lineTo(9.293f, 13.293f)
                curveTo(9.683f, 12.902f, 10.317f, 12.902f, 10.707f, 13.293f)
                curveTo(11.098f, 13.683f, 11.098f, 14.317f, 10.707f, 14.707f)
                lineTo(6.414f, 19f)
                horizontalLineTo(11f)
                curveTo(11.552f, 19f, 12f, 19.448f, 12f, 20f)
                curveTo(12f, 20.552f, 11.552f, 21f, 11f, 21f)
                horizontalLineTo(4.001f)
                lineTo(3.997f, 21f)
                curveTo(3.862f, 21f, 3.734f, 20.973f, 3.617f, 20.924f)
                curveTo(3.499f, 20.875f, 3.389f, 20.803f, 3.293f, 20.707f)
                curveTo(3.197f, 20.611f, 3.125f, 20.501f, 3.076f, 20.383f)
                curveTo(3.027f, 20.265f, 3f, 20.136f, 3f, 20f)
                verticalLineTo(13f)
                curveTo(3f, 12.448f, 3.448f, 12f, 4f, 12f)
                close()
            }
        }.build()

        return checkNotNull(_Expand) { "_Expand must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Expand: ImageVector? = null

val Collapse: ImageVector
    get() {
        _Collapse?.let { return it }
        _Collapse = ImageVector.Builder(
            name = "Collapse",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(13f, 3f)
                curveTo(13.552f, 3f, 14f, 3.448f, 14f, 4f)
                verticalLineTo(8.586f)
                lineTo(18.293f, 4.293f)
                curveTo(18.683f, 3.902f, 19.317f, 3.902f, 19.707f, 4.293f)
                curveTo(20.098f, 4.683f, 20.098f, 5.317f, 19.707f, 5.707f)
                lineTo(15.414f, 10f)
                horizontalLineTo(20f)
                curveTo(20.552f, 10f, 21f, 10.448f, 21f, 11f)
                curveTo(21f, 11.552f, 20.552f, 12f, 20f, 12f)
                horizontalLineTo(13.001f)
                lineTo(12.997f, 12f)
                curveTo(12.863f, 12f, 12.734f, 11.973f, 12.617f, 11.924f)
                curveTo(12.499f, 11.875f, 12.389f, 11.803f, 12.293f, 11.707f)
                curveTo(12.197f, 11.611f, 12.125f, 11.501f, 12.076f, 11.383f)
                curveTo(12.027f, 11.265f, 12f, 11.136f, 12f, 11f)
                verticalLineTo(4f)
                curveTo(12f, 3.448f, 12.448f, 3f, 13f, 3f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 21f)
                curveTo(10.448f, 21f, 10f, 20.552f, 10f, 20f)
                lineTo(10f, 15.414f)
                lineTo(5.707f, 19.707f)
                curveTo(5.317f, 20.098f, 4.683f, 20.098f, 4.293f, 19.707f)
                curveTo(3.902f, 19.317f, 3.902f, 18.683f, 4.293f, 18.293f)
                lineTo(8.586f, 14f)
                lineTo(4f, 14f)
                curveTo(3.448f, 14f, 3f, 13.552f, 3f, 13f)
                curveTo(3f, 12.448f, 3.448f, 12f, 4f, 12f)
                lineTo(10.999f, 12f)
                lineTo(11.003f, 12f)
                curveTo(11.137f, 12f, 11.266f, 12.027f, 11.383f, 12.076f)
                curveTo(11.501f, 12.125f, 11.611f, 12.197f, 11.707f, 12.293f)
                curveTo(11.803f, 12.389f, 11.875f, 12.499f, 11.924f, 12.617f)
                curveTo(11.973f, 12.735f, 12f, 12.864f, 12f, 13f)
                lineTo(12f, 20f)
                curveTo(12f, 20.552f, 11.552f, 21f, 11f, 21f)
                close()
            }
        }.build()

        return checkNotNull(_Collapse) { "_Collapse must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Collapse: ImageVector? = null
