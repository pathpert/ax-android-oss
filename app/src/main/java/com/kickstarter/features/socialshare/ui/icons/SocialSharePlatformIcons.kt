package com.kickstarter.features.socialshare.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SocialShareWhatsApp: ImageVector
    get() {
        _SocialShareWhatsApp?.let { return it }
        _SocialShareWhatsApp = ImageVector.Builder(
            name = "SocialShareWhatsApp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF25D366)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(20.503f, 3.485f)
                curveTo(18.247f, 1.239f, 15.247f, 0.001f, 12.05f, 0f)
                curveTo(5.463f, 0f, 0.103f, 5.331f, 0.1f, 11.883f)
                curveTo(0.099f, 13.978f, 0.65f, 16.022f, 1.695f, 17.824f)
                lineTo(0f, 23.983f)
                lineTo(6.335f, 22.33f)
                curveTo(8.08f, 23.277f, 10.045f, 23.776f, 12.045f, 23.776f)
                horizontalLineTo(12.05f)
                curveTo(18.636f, 23.776f, 23.998f, 18.445f, 24f, 11.893f)
                curveTo(24.001f, 8.717f, 22.76f, 5.732f, 20.503f, 3.486f)
                verticalLineTo(3.485f)
                close()
                moveTo(12.05f, 21.769f)
                horizontalLineTo(12.046f)
                curveTo(10.264f, 21.769f, 8.516f, 21.292f, 6.991f, 20.393f)
                lineTo(6.628f, 20.178f)
                lineTo(2.869f, 21.159f)
                lineTo(3.872f, 17.514f)
                lineTo(3.636f, 17.14f)
                curveTo(2.642f, 15.567f, 2.117f, 13.75f, 2.118f, 11.884f)
                curveTo(2.12f, 6.438f, 6.576f, 2.007f, 12.054f, 2.007f)
                curveTo(14.707f, 2.008f, 17.201f, 3.037f, 19.076f, 4.904f)
                curveTo(20.952f, 6.771f, 21.984f, 9.253f, 21.983f, 11.892f)
                curveTo(21.98f, 17.338f, 17.525f, 21.769f, 12.05f, 21.769f)
                verticalLineTo(21.769f)
                close()
                moveTo(17.498f, 14.372f)
                curveTo(17.2f, 14.223f, 15.732f, 13.505f, 15.458f, 13.406f)
                curveTo(15.184f, 13.307f, 14.985f, 13.257f, 14.786f, 13.555f)
                curveTo(14.588f, 13.852f, 14.015f, 14.521f, 13.841f, 14.719f)
                curveTo(13.667f, 14.917f, 13.493f, 14.942f, 13.194f, 14.793f)
                curveTo(12.896f, 14.644f, 11.934f, 14.331f, 10.793f, 13.319f)
                curveTo(9.905f, 12.532f, 9.306f, 11.56f, 9.132f, 11.262f)
                curveTo(8.957f, 10.965f, 9.113f, 10.804f, 9.262f, 10.657f)
                curveTo(9.396f, 10.523f, 9.561f, 10.31f, 9.71f, 10.137f)
                curveTo(9.86f, 9.963f, 9.909f, 9.839f, 10.009f, 9.641f)
                curveTo(10.109f, 9.443f, 10.059f, 9.27f, 9.984f, 9.121f)
                curveTo(9.91f, 8.972f, 9.313f, 7.511f, 9.063f, 6.916f)
                curveTo(8.821f, 6.338f, 8.575f, 6.416f, 8.392f, 6.406f)
                curveTo(8.218f, 6.398f, 8.019f, 6.396f, 7.82f, 6.396f)
                curveTo(7.62f, 6.396f, 7.297f, 6.47f, 7.023f, 6.768f)
                curveTo(6.749f, 7.065f, 5.978f, 7.784f, 5.978f, 9.245f)
                curveTo(5.978f, 10.706f, 7.048f, 12.118f, 7.197f, 12.317f)
                curveTo(7.347f, 12.515f, 9.303f, 15.514f, 12.298f, 16.801f)
                curveTo(13.01f, 17.107f, 13.566f, 17.289f, 14f, 17.426f)
                curveTo(14.715f, 17.652f, 15.366f, 17.621f, 15.881f, 17.544f)
                curveTo(16.454f, 17.459f, 17.647f, 16.826f, 17.896f, 16.132f)
                curveTo(18.145f, 15.439f, 18.145f, 14.844f, 18.07f, 14.72f)
                curveTo(17.996f, 14.596f, 17.796f, 14.522f, 17.498f, 14.373f)
                lineTo(17.498f, 14.372f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareWhatsApp) { "_SocialShareWhatsApp must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareWhatsApp: ImageVector? = null

val SocialShareX: ImageVector
    get() {
        _SocialShareX?.let { return it }
        _SocialShareX = ImageVector.Builder(
            name = "SocialShareX",
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
                path(fill = SolidColor(Color(0xFF3C3C3C))) {
                    moveTo(14.283f, 10.157f)
                    lineTo(23.218f, 0f)
                    horizontalLineTo(21.101f)
                    lineTo(13.343f, 8.819f)
                    lineTo(7.147f, 0f)
                    horizontalLineTo(0f)
                    lineTo(9.37f, 13.336f)
                    lineTo(0f, 23.988f)
                    horizontalLineTo(2.117f)
                    lineTo(10.31f, 14.674f)
                    lineTo(16.853f, 23.988f)
                    horizontalLineTo(24f)
                    lineTo(14.283f, 10.157f)
                    lineTo(14.283f, 10.157f)
                    close()
                    moveTo(11.383f, 13.454f)
                    lineTo(10.434f, 12.126f)
                    lineTo(2.88f, 1.559f)
                    horizontalLineTo(6.132f)
                    lineTo(12.228f, 10.087f)
                    lineTo(13.178f, 11.415f)
                    lineTo(21.102f, 22.5f)
                    horizontalLineTo(17.85f)
                    lineTo(11.383f, 13.454f)
                    verticalLineTo(13.454f)
                    close()
                }
            }
        }.build()

        return checkNotNull(_SocialShareX) { "_SocialShareX must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareX: ImageVector? = null
