package com.kickstarter.features.socialshare.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SocialShareMore: ImageVector
    get() {
        _SocialShareMore?.let { return it }
        _SocialShareMore = ImageVector.Builder(
            name = "SocialShareMore",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Left dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
            // Right dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
            // Center dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareMore) { "_SocialShareMore must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareMore: ImageVector? = null

val SocialShareEmail: ImageVector
    get() {
        _SocialShareEmail?.let { return it }
        _SocialShareEmail = ImageVector.Builder(
            name = "SocialShareEmail",
            defaultWidth = 20.dp,
            defaultHeight = 14.dp,
            viewportWidth = 20f,
            viewportHeight = 14f
        ).apply {
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(17f, 0f)
                curveTo(18.657f, 0f, 20f, 1.343f, 20f, 3f)
                verticalLineTo(11f)
                curveTo(20f, 12.657f, 18.657f, 14f, 17f, 14f)
                horizontalLineTo(3f)
                curveTo(1.343f, 14f, 0f, 12.657f, 0f, 11f)
                verticalLineTo(3f)
                curveTo(0f, 1.343f, 1.343f, 0f, 3f, 0f)
                horizontalLineTo(17f)
                close()
                moveTo(10.636f, 9.771f)
                curveTo(10.267f, 10.076f, 9.734f, 10.076f, 9.364f, 9.771f)
                lineTo(2f, 3.706f)
                verticalLineTo(11f)
                curveTo(2f, 11.552f, 2.448f, 12f, 3f, 12f)
                horizontalLineTo(17f)
                curveTo(17.552f, 12f, 18f, 11.552f, 18f, 11f)
                verticalLineTo(3.706f)
                lineTo(10.636f, 9.771f)
                close()
                moveTo(10f, 7.704f)
                lineTo(16.928f, 2f)
                horizontalLineTo(3.072f)
                lineTo(10f, 7.704f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareEmail) { "_SocialShareEmail must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareEmail: ImageVector? = null

val SocialShareMessage: ImageVector
    get() {
        _SocialShareMessage?.let { return it }
        _SocialShareMessage = ImageVector.Builder(
            name = "SocialShareMessage",
            defaultWidth = 27.dp,
            defaultHeight = 25.dp,
            viewportWidth = 27f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(5.086f, 24.551f)
                curveTo(4.68f, 24.551f, 4.371f, 24.453f, 4.16f, 24.258f)
                curveTo(3.949f, 24.07f, 3.84f, 23.832f, 3.832f, 23.543f)
                curveTo(3.824f, 23.262f, 3.922f, 22.984f, 4.125f, 22.711f)
                curveTo(4.305f, 22.484f, 4.52f, 22.191f, 4.77f, 21.832f)
                curveTo(5.02f, 21.48f, 5.27f, 21.109f, 5.52f, 20.719f)
                curveTo(5.77f, 20.328f, 5.984f, 19.961f, 6.164f, 19.617f)
                curveTo(4.93f, 19.047f, 3.848f, 18.305f, 2.918f, 17.391f)
                curveTo(1.996f, 16.477f, 1.277f, 15.445f, 0.762f, 14.297f)
                curveTo(0.254f, 13.148f, 0f, 11.934f, 0f, 10.652f)
                curveTo(0f, 9.176f, 0.34f, 7.793f, 1.02f, 6.504f)
                curveTo(1.707f, 5.215f, 2.656f, 4.086f, 3.867f, 3.117f)
                curveTo(5.086f, 2.141f, 6.5f, 1.379f, 8.109f, 0.832f)
                curveTo(9.719f, 0.277f, 11.445f, 0f, 13.289f, 0f)
                curveTo(15.133f, 0f, 16.859f, 0.277f, 18.469f, 0.832f)
                curveTo(20.078f, 1.379f, 21.488f, 2.141f, 22.699f, 3.117f)
                curveTo(23.918f, 4.086f, 24.867f, 5.215f, 25.547f, 6.504f)
                curveTo(26.234f, 7.793f, 26.578f, 9.176f, 26.578f, 10.652f)
                curveTo(26.578f, 11.879f, 26.344f, 13.039f, 25.875f, 14.133f)
                curveTo(25.414f, 15.219f, 24.754f, 16.207f, 23.895f, 17.098f)
                curveTo(23.043f, 17.988f, 22.023f, 18.754f, 20.836f, 19.395f)
                curveTo(19.656f, 20.027f, 18.344f, 20.508f, 16.898f, 20.836f)
                curveTo(15.453f, 21.164f, 13.914f, 21.305f, 12.281f, 21.258f)
                curveTo(11.492f, 21.828f, 10.641f, 22.363f, 9.727f, 22.863f)
                curveTo(8.813f, 23.363f, 7.941f, 23.77f, 7.113f, 24.082f)
                curveTo(6.285f, 24.395f, 5.609f, 24.551f, 5.086f, 24.551f)
                close()
                moveTo(6.316f, 22.605f)
                curveTo(6.668f, 22.457f, 7.113f, 22.23f, 7.652f, 21.926f)
                curveTo(8.191f, 21.621f, 8.75f, 21.281f, 9.328f, 20.906f)
                curveTo(9.906f, 20.539f, 10.434f, 20.184f, 10.91f, 19.84f)
                curveTo(11.129f, 19.668f, 11.34f, 19.543f, 11.543f, 19.465f)
                curveTo(11.754f, 19.379f, 11.981f, 19.336f, 12.223f, 19.336f)
                curveTo(12.457f, 19.344f, 12.664f, 19.352f, 12.844f, 19.359f)
                curveTo(13.023f, 19.359f, 13.172f, 19.359f, 13.289f, 19.359f)
                curveTo(14.867f, 19.359f, 16.344f, 19.133f, 17.719f, 18.68f)
                curveTo(19.094f, 18.227f, 20.297f, 17.602f, 21.328f, 16.805f)
                curveTo(22.367f, 16.008f, 23.18f, 15.082f, 23.766f, 14.027f)
                curveTo(24.352f, 12.973f, 24.645f, 11.848f, 24.645f, 10.652f)
                curveTo(24.645f, 9.449f, 24.352f, 8.324f, 23.766f, 7.277f)
                curveTo(23.18f, 6.223f, 22.367f, 5.297f, 21.328f, 4.5f)
                curveTo(20.297f, 3.695f, 19.094f, 3.066f, 17.719f, 2.613f)
                curveTo(16.344f, 2.16f, 14.867f, 1.934f, 13.289f, 1.934f)
                curveTo(11.711f, 1.934f, 10.234f, 2.16f, 8.859f, 2.613f)
                curveTo(7.484f, 3.066f, 6.277f, 3.695f, 5.238f, 4.5f)
                curveTo(4.207f, 5.297f, 3.398f, 6.223f, 2.813f, 7.277f)
                curveTo(2.234f, 8.324f, 1.945f, 9.449f, 1.945f, 10.652f)
                curveTo(1.945f, 11.691f, 2.172f, 12.68f, 2.625f, 13.617f)
                curveTo(3.078f, 14.555f, 3.734f, 15.414f, 4.594f, 16.195f)
                curveTo(5.461f, 16.969f, 6.508f, 17.641f, 7.734f, 18.211f)
                curveTo(8.109f, 18.391f, 8.328f, 18.609f, 8.391f, 18.867f)
                curveTo(8.453f, 19.125f, 8.402f, 19.406f, 8.238f, 19.711f)
                curveTo(8.035f, 20.086f, 7.734f, 20.539f, 7.336f, 21.07f)
                curveTo(6.938f, 21.609f, 6.574f, 22.082f, 6.246f, 22.488f)
                curveTo(6.215f, 22.527f, 6.207f, 22.559f, 6.223f, 22.582f)
                curveTo(6.238f, 22.613f, 6.27f, 22.621f, 6.316f, 22.605f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareMessage) { "_SocialShareMessage must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareMessage: ImageVector? = null

val SocialShareCopyLink: ImageVector
    get() {
        _SocialShareCopyLink?.let { return it }
        _SocialShareCopyLink = ImageVector.Builder(
            name = "Link",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF3C3C3C)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.465f, 9.877f)
                curveTo(8.075f, 9.486f, 7.441f, 9.486f, 7.051f, 9.877f)
                lineTo(4.929f, 11.999f)
                curveTo(2.976f, 13.952f, 2.976f, 17.118f, 4.929f, 19.07f)
                curveTo(6.881f, 21.023f, 10.047f, 21.023f, 12f, 19.07f)
                lineTo(14.122f, 16.948f)
                curveTo(14.512f, 16.557f, 14.512f, 15.924f, 14.122f, 15.534f)
                curveTo(13.731f, 15.143f, 13.098f, 15.143f, 12.708f, 15.534f)
                lineTo(10.585f, 17.656f)
                curveTo(9.414f, 18.827f, 7.514f, 18.827f, 6.343f, 17.656f)
                curveTo(5.171f, 16.484f, 5.171f, 14.585f, 6.343f, 13.413f)
                lineTo(8.465f, 11.291f)
                curveTo(8.856f, 10.901f, 8.856f, 10.267f, 8.465f, 9.877f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3C3C3C)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15.535f, 14.121f)
                curveTo(15.925f, 14.512f, 16.558f, 14.512f, 16.949f, 14.121f)
                lineTo(19.071f, 11.999f)
                curveTo(21.024f, 10.046f, 21.024f, 6.881f, 19.071f, 4.928f)
                curveTo(17.119f, 2.975f, 13.953f, 2.975f, 12f, 4.928f)
                lineTo(9.878f, 7.05f)
                curveTo(9.487f, 7.441f, 9.487f, 8.074f, 9.878f, 8.464f)
                curveTo(10.268f, 8.855f, 10.902f, 8.855f, 11.292f, 8.464f)
                lineTo(13.414f, 6.342f)
                curveTo(14.586f, 5.171f, 16.486f, 5.171f, 17.657f, 6.342f)
                curveTo(18.829f, 7.514f, 18.829f, 9.413f, 17.657f, 10.585f)
                lineTo(15.535f, 12.707f)
                curveTo(15.144f, 13.098f, 15.144f, 13.731f, 15.535f, 14.121f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(14.828f, 7.756f)
                curveTo(15.219f, 7.365f, 15.852f, 7.365f, 16.242f, 7.756f)
                curveTo(16.633f, 8.146f, 16.633f, 8.78f, 16.242f, 9.17f)
                lineTo(9.171f, 16.241f)
                curveTo(8.781f, 16.632f, 8.147f, 16.632f, 7.757f, 16.241f)
                curveTo(7.366f, 15.851f, 7.366f, 15.217f, 7.757f, 14.827f)
                lineTo(14.828f, 7.756f)
                close()
            }
        }.build()

        return checkNotNull(_SocialShareCopyLink) { "_SocialShareCopyLink must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _SocialShareCopyLink: ImageVector? = null

