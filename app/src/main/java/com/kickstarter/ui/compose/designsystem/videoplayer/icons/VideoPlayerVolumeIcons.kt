package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Mute: ImageVector
    get() {
        _Mute?.let { return it }
        _Mute = ImageVector.Builder(
            name = "Mute",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFE0E0E0)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6f, 9f)
                horizontalLineTo(7.705f)
                curveTo(8.597f, 9f, 9.435f, 8.604f, 10f, 7.932f)
                lineTo(10f, 16.068f)
                curveTo(9.435f, 15.396f, 8.597f, 15f, 7.705f, 15f)
                horizontalLineTo(6f)
                lineTo(6f, 9f)
                close()
                moveTo(7.705f, 7f)
                curveTo(8.05f, 7f, 8.371f, 6.822f, 8.553f, 6.529f)
                lineTo(10.151f, 3.966f)
                curveTo(10.683f, 3.112f, 12f, 3.489f, 12f, 4.495f)
                lineTo(12f, 19.506f)
                curveTo(12f, 20.511f, 10.683f, 20.888f, 10.151f, 20.035f)
                lineTo(8.553f, 17.471f)
                curveTo(8.371f, 17.178f, 8.05f, 17f, 7.705f, 17f)
                horizontalLineTo(5f)
                curveTo(4.448f, 17f, 4f, 16.552f, 4f, 16f)
                lineTo(4f, 8f)
                curveTo(4f, 7.448f, 4.448f, 7f, 5f, 7f)
                horizontalLineTo(7.705f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE0E0E0))) {
                moveTo(18.414f, 14.828f)
                curveTo(18.805f, 15.219f, 19.438f, 15.219f, 19.828f, 14.828f)
                curveTo(20.219f, 14.438f, 20.219f, 13.804f, 19.828f, 13.414f)
                lineTo(15.586f, 9.171f)
                curveTo(15.195f, 8.781f, 14.562f, 8.781f, 14.171f, 9.171f)
                curveTo(13.781f, 9.562f, 13.781f, 10.195f, 14.171f, 10.585f)
                lineTo(18.414f, 14.828f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE0E0E0))) {
                moveTo(19.828f, 10.586f)
                curveTo(20.219f, 10.196f, 20.219f, 9.562f, 19.828f, 9.172f)
                curveTo(19.438f, 8.781f, 18.804f, 8.781f, 18.414f, 9.172f)
                lineTo(14.171f, 13.415f)
                curveTo(13.781f, 13.805f, 13.781f, 14.438f, 14.171f, 14.829f)
                curveTo(14.562f, 15.219f, 15.195f, 15.219f, 15.585f, 14.829f)
                lineTo(19.828f, 10.586f)
                close()
            }
        }.build()

        return checkNotNull(_Mute) { "_Mute must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Mute: ImageVector? = null

val VolumeUp: ImageVector
    get() {
        _VolumeUp?.let { return it }
        _VolumeUp = ImageVector.Builder(
            name = "VolumeUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFE0E0E0)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6f, 9f)
                horizontalLineTo(7.705f)
                curveTo(8.597f, 9f, 9.435f, 8.604f, 10f, 7.932f)
                lineTo(10f, 16.068f)
                curveTo(9.435f, 15.396f, 8.597f, 15f, 7.705f, 15f)
                horizontalLineTo(6f)
                lineTo(6f, 9f)
                close()
                moveTo(7.705f, 7f)
                curveTo(8.05f, 7f, 8.371f, 6.822f, 8.553f, 6.529f)
                lineTo(10.151f, 3.966f)
                curveTo(10.683f, 3.112f, 12f, 3.489f, 12f, 4.495f)
                lineTo(12f, 19.506f)
                curveTo(12f, 20.511f, 10.683f, 20.888f, 10.151f, 20.035f)
                lineTo(8.553f, 17.471f)
                curveTo(8.371f, 17.178f, 8.05f, 17f, 7.705f, 17f)
                horizontalLineTo(5f)
                curveTo(4.448f, 17f, 4f, 16.552f, 4f, 16f)
                lineTo(4f, 8f)
                curveTo(4f, 7.448f, 4.448f, 7f, 5f, 7f)
                horizontalLineTo(7.705f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE0E0E0))) {
                moveTo(16.491f, 18.861f)
                curveTo(16.966f, 19.142f, 17.579f, 18.985f, 17.861f, 18.509f)
                lineTo(17f, 18f)
                curveTo(17.861f, 18.509f, 17.861f, 18.509f, 17.861f, 18.509f)
                lineTo(17.864f, 18.504f)
                lineTo(17.869f, 18.495f)
                lineTo(17.889f, 18.461f)
                curveTo(17.905f, 18.432f, 17.929f, 18.391f, 17.958f, 18.339f)
                curveTo(18.017f, 18.234f, 18.1f, 18.083f, 18.198f, 17.895f)
                curveTo(18.395f, 17.52f, 18.657f, 16.991f, 18.92f, 16.375f)
                curveTo(19.432f, 15.173f, 20f, 13.519f, 20f, 12f)
                curveTo(20f, 10.481f, 19.432f, 8.827f, 18.92f, 7.625f)
                curveTo(18.657f, 7.009f, 18.395f, 6.48f, 18.198f, 6.105f)
                curveTo(18.1f, 5.917f, 18.017f, 5.766f, 17.958f, 5.661f)
                curveTo(17.929f, 5.609f, 17.905f, 5.568f, 17.889f, 5.539f)
                lineTo(17.869f, 5.505f)
                lineTo(17.864f, 5.496f)
                lineTo(17.861f, 5.492f)
                curveTo(17.861f, 5.492f, 17.861f, 5.491f, 17f, 6f)
                lineTo(17.861f, 5.492f)
                curveTo(17.58f, 5.016f, 16.966f, 4.858f, 16.491f, 5.139f)
                curveTo(16.016f, 5.421f, 15.858f, 6.034f, 16.139f, 6.509f)
                lineTo(16.142f, 6.514f)
                lineTo(16.156f, 6.538f)
                curveTo(16.169f, 6.56f, 16.188f, 6.594f, 16.214f, 6.639f)
                curveTo(16.264f, 6.73f, 16.338f, 6.864f, 16.427f, 7.033f)
                curveTo(16.605f, 7.373f, 16.843f, 7.853f, 17.08f, 8.41f)
                curveTo(17.568f, 9.554f, 18f, 10.9f, 18f, 12f)
                curveTo(18f, 13.1f, 17.568f, 14.446f, 17.08f, 15.59f)
                curveTo(16.843f, 16.147f, 16.605f, 16.627f, 16.427f, 16.967f)
                curveTo(16.338f, 17.136f, 16.264f, 17.271f, 16.214f, 17.361f)
                curveTo(16.188f, 17.406f, 16.169f, 17.44f, 16.156f, 17.462f)
                lineTo(16.142f, 17.486f)
                lineTo(16.139f, 17.491f)
                moveTo(16.491f, 18.861f)
                curveTo(16.016f, 18.58f, 15.858f, 17.966f, 16.139f, 17.491f)
                lineTo(16.491f, 18.861f)
                close()
                moveTo(13.468f, 15.847f)
                curveTo(13.936f, 16.141f, 14.553f, 16f, 14.847f, 15.532f)
                lineTo(14f, 15f)
                curveTo(14.847f, 15.532f, 14.847f, 15.532f, 14.847f, 15.532f)
                lineTo(14.848f, 15.53f)
                lineTo(14.849f, 15.528f)
                lineTo(14.853f, 15.522f)
                lineTo(14.865f, 15.503f)
                curveTo(14.875f, 15.487f, 14.888f, 15.465f, 14.904f, 15.438f)
                curveTo(14.936f, 15.383f, 14.981f, 15.306f, 15.033f, 15.21f)
                curveTo(15.138f, 15.018f, 15.277f, 14.748f, 15.417f, 14.427f)
                curveTo(15.686f, 13.81f, 16f, 12.91f, 16f, 12f)
                curveTo(16f, 11.09f, 15.686f, 10.19f, 15.417f, 9.573f)
                curveTo(15.277f, 9.252f, 15.138f, 8.982f, 15.033f, 8.791f)
                curveTo(14.981f, 8.694f, 14.936f, 8.617f, 14.904f, 8.563f)
                curveTo(14.888f, 8.535f, 14.875f, 8.513f, 14.865f, 8.497f)
                lineTo(14.853f, 8.478f)
                lineTo(14.849f, 8.472f)
                lineTo(14.847f, 8.469f)
                curveTo(14.847f, 8.469f, 14.847f, 8.468f, 14f, 9f)
                lineTo(14.847f, 8.469f)
                curveTo(14.554f, 8.001f, 13.936f, 7.859f, 13.468f, 8.153f)
                curveTo(13.001f, 8.447f, 12.86f, 9.063f, 13.152f, 9.531f)
                lineTo(13.153f, 9.532f)
                lineTo(13.158f, 9.539f)
                curveTo(13.163f, 9.547f, 13.171f, 9.561f, 13.182f, 9.58f)
                curveTo(13.204f, 9.618f, 13.238f, 9.676f, 13.279f, 9.751f)
                curveTo(13.362f, 9.902f, 13.473f, 10.118f, 13.583f, 10.371f)
                curveTo(13.814f, 10.902f, 14f, 11.502f, 14f, 12f)
                curveTo(14f, 12.498f, 13.814f, 13.098f, 13.583f, 13.629f)
                curveTo(13.473f, 13.882f, 13.362f, 14.098f, 13.279f, 14.249f)
                curveTo(13.238f, 14.324f, 13.204f, 14.382f, 13.182f, 14.42f)
                curveTo(13.171f, 14.439f, 13.163f, 14.453f, 13.158f, 14.461f)
                lineTo(13.153f, 14.468f)
                curveTo(12.86f, 14.936f, 13f, 15.553f, 13.468f, 15.847f)
                close()
            }
        }.build()

        return checkNotNull(_VolumeUp) { "_VolumeUp must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _VolumeUp: ImageVector? = null

