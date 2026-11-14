package com.kickstarter.features.socialshare.ui.icons

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

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview @Composable private fun CopyLinkPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareCopyLink, null) } }
@Preview @Composable private fun InstagramPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareInstagram, null) } }
@Preview @Composable private fun XPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareX, null) } }
@Preview @Composable private fun FacebookPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareFacebook, null) } }
@Preview @Composable private fun WhatsAppPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareWhatsApp, null) } }
@Preview @Composable private fun MessagePreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareMessage, null) } }
@Preview @Composable private fun EmailPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareEmail, null) } }
@Preview @Composable private fun MorePreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareMore, null) } }
@Preview @Composable private fun NoClearancePreview() { Box(modifier = Modifier.padding(12.dp)) { Image(imageVector = KSLogo, contentDescription = null) } }

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------

val Vector91: ImageVector
    get() {
        _Vector91?.let { return it }
        _Vector91 = ImageVector.Builder(
            name = "Vector91",
            defaultWidth = 393.dp,
            defaultHeight = 728.dp,
            viewportWidth = 393f,
            viewportHeight = 728f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF9BFF1D)),
                strokeLineWidth = 120f
            ) {
                moveTo(262.59f, 1498.33f)
                curveTo(500.69f, 1465.14f, 609.41f, 1189.46f, 211.07f, 991.26f)
                curveTo(586.58f, 1036.13f, 529.5f, 722.57f, 278.4f, 463.14f)
                curveTo(165.04f, 346.02f, -21.39f, 195.2f, 88.27f, 36.96f)
                curveTo(187.71f, -106.54f, 299.17f, 119.43f, 125.28f, 90.54f)
                curveTo(-21.18f, 66.2f, -151.93f, -139.73f, 10.72f, -446.05f)
            }
        }.build()

        return checkNotNull(_Vector91) { "_Vector91 must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _Vector91: ImageVector? = null

@Preview
@Composable
private fun Vector91Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Vector91, contentDescription = null)
    }
}

val KSLogo: ImageVector
    get() {
        _KSLogo?.let { return it }
        _KSLogo = ImageVector.Builder(
            name = "KSLogo",
            defaultWidth = 252.dp,
            defaultHeight = 28.dp,
            viewportWidth = 252f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color(0xFF05CE78))) {
                moveTo(100.21f, 0f)
                curveTo(106.8f, 0f, 109.91f, 2.44f, 109.91f, 6.53f)
                curveTo(109.91f, 8.97f, 108.33f, 10.62f, 105.96f, 10.99f)
                lineTo(107.59f, 12.51f)
                curveTo(109.56f, 14.35f, 110.88f, 16.28f, 110.88f, 19.04f)
                curveTo(110.88f, 24.51f, 106.84f, 27.96f, 100.03f, 27.96f)
                curveTo(93.09f, 27.96f, 89.84f, 25.47f, 89.84f, 21.43f)
                curveTo(89.84f, 18.62f, 91.91f, 16.83f, 94.85f, 16.83f)
                horizontalLineTo(94.89f)
                lineTo(93.31f, 15.31f)
                curveTo(91.34f, 13.43f, 89.98f, 11.54f, 89.98f, 8.78f)
                curveTo(89.98f, 3.4f, 93.75f, 0f, 100.21f, 0f)
                close()
                moveTo(128.44f, 0.46f)
                curveTo(131.56f, 0.46f, 134.11f, 3.07f, 134.11f, 6.37f)
                curveTo(134.11f, 9.58f, 131.69f, 12.15f, 128.75f, 12.28f)
                verticalLineTo(21.77f)
                curveTo(128.75f, 25.16f, 125.98f, 27.96f, 122.43f, 27.96f)
                curveTo(119f, 27.96f, 116.24f, 25.16f, 116.24f, 21.77f)
                verticalLineTo(12.28f)
                curveTo(113.29f, 12.1f, 110.88f, 9.58f, 110.88f, 6.37f)
                curveTo(110.88f, 3.02f, 113.43f, 0.46f, 116.54f, 0.46f)
                horizontalLineTo(128.44f)
                close()
                moveTo(172.7f, 0.46f)
                curveTo(177.33f, 0.46f, 180.95f, 4.13f, 180.95f, 8.85f)
                curveTo(180.95f, 11.41f, 179.94f, 13.52f, 178.26f, 14.85f)
                lineTo(180.56f, 18.65f)
                curveTo(181.18f, 19.66f, 181.44f, 20.76f, 181.44f, 21.91f)
                curveTo(181.44f, 25.3f, 178.88f, 27.96f, 175.65f, 27.96f)
                curveTo(173.67f, 27.96f, 171.72f, 26.9f, 170.62f, 24.98f)
                lineTo(170f, 23.83f)
                curveTo(169.25f, 26.26f, 167.17f, 27.96f, 164.52f, 27.96f)
                curveTo(161.3f, 27.96f, 158.65f, 25.11f, 158.65f, 21.68f)
                verticalLineTo(7.29f)
                curveTo(158.65f, 2.94f, 160.81f, 0.61f, 164.68f, 0.46f)
                lineTo(165.05f, 0.46f)
                horizontalLineTo(172.7f)
                close()
                moveTo(199.45f, 0.46f)
                curveTo(202.56f, 0.46f, 205.11f, 3.07f, 205.11f, 6.37f)
                curveTo(205.11f, 9.58f, 202.7f, 12.15f, 199.76f, 12.28f)
                verticalLineTo(21.77f)
                curveTo(199.76f, 25.16f, 197.04f, 27.96f, 193.4f, 27.96f)
                curveTo(189.99f, 27.96f, 187.23f, 25.16f, 187.23f, 21.77f)
                verticalLineTo(12.28f)
                curveTo(184.29f, 12.1f, 181.88f, 9.58f, 181.88f, 6.37f)
                curveTo(181.88f, 3.13f, 184.26f, 0.62f, 187.24f, 0.47f)
                lineTo(187.53f, 0.46f)
                horizontalLineTo(199.45f)
                close()
                moveTo(243.26f, 0.46f)
                curveTo(247.89f, 0.46f, 251.51f, 4.13f, 251.51f, 8.85f)
                curveTo(251.51f, 11.41f, 250.5f, 13.52f, 248.82f, 14.85f)
                lineTo(251.12f, 18.65f)
                curveTo(251.74f, 19.66f, 252f, 20.76f, 252f, 21.91f)
                curveTo(252f, 25.3f, 249.44f, 27.96f, 246.21f, 27.96f)
                curveTo(244.23f, 27.96f, 242.28f, 26.9f, 241.18f, 24.98f)
                lineTo(240.56f, 23.83f)
                curveTo(239.81f, 26.26f, 237.74f, 27.96f, 235.09f, 27.96f)
                curveTo(231.86f, 27.96f, 229.21f, 25.12f, 229.21f, 21.68f)
                verticalLineTo(7.29f)
                curveTo(229.21f, 2.8f, 231.46f, 0.46f, 235.61f, 0.46f)
                horizontalLineTo(243.26f)
                close()
                moveTo(222.45f, 0.46f)
                curveTo(225.19f, 0.46f, 227.46f, 2.82f, 227.46f, 5.77f)
                curveTo(227.46f, 8.45f, 225.89f, 10.39f, 223.63f, 10.86f)
                curveTo(224.67f, 11.55f, 225.24f, 12.71f, 225.24f, 14.23f)
                curveTo(225.24f, 15.76f, 224.63f, 16.91f, 223.63f, 17.6f)
                curveTo(225.85f, 18.07f, 227.46f, 20.05f, 227.46f, 22.69f)
                curveTo(227.46f, 25.65f, 225.19f, 28f, 222.45f, 27.96f)
                horizontalLineTo(213.3f)
                lineTo(212.92f, 27.95f)
                curveTo(208.98f, 27.81f, 206.86f, 25.5f, 206.86f, 21.12f)
                verticalLineTo(7.3f)
                curveTo(206.86f, 2.82f, 209.12f, 0.46f, 213.3f, 0.46f)
                horizontalLineTo(222.45f)
                close()
                moveTo(31.8f, 0f)
                curveTo(35.34f, 0f, 38.17f, 3.02f, 38.13f, 6.58f)
                verticalLineTo(20.92f)
                curveTo(38.13f, 24.48f, 35.34f, 27.5f, 31.75f, 27.5f)
                curveTo(28.25f, 27.5f, 25.42f, 24.53f, 25.42f, 20.92f)
                verticalLineTo(6.58f)
                curveTo(25.42f, 3.02f, 28.25f, 0f, 31.8f, 0f)
                close()
                moveTo(53.48f, 0f)
                curveTo(59.14f, 0f, 62.67f, 3.14f, 62.67f, 7.56f)
                curveTo(62.67f, 10.52f, 61.28f, 12.52f, 58.49f, 13.75f)
                curveTo(61.28f, 14.98f, 62.67f, 17.03f, 62.67f, 19.94f)
                curveTo(62.67f, 24.36f, 59.14f, 27.5f, 53.48f, 27.5f)
                curveTo(45.9f, 27.5f, 40.32f, 21.63f, 40.32f, 13.75f)
                curveTo(40.32f, 5.87f, 45.9f, 0f, 53.48f, 0f)
                close()
                moveTo(144.2f, 0f)
                curveTo(147.58f, 0f, 149.56f, 1.78f, 150.88f, 5.01f)
                lineTo(156.29f, 18.12f)
                curveTo(158.31f, 23.08f, 155.01f, 27.5f, 150.35f, 27.5f)
                curveTo(147.76f, 27.5f, 145.65f, 25.95f, 144.15f, 22.72f)
                curveTo(142.66f, 25.95f, 140.55f, 27.5f, 138f, 27.5f)
                curveTo(133.38f, 27.5f, 130.04f, 23.08f, 132.1f, 18.12f)
                lineTo(137.51f, 5.01f)
                curveTo(138.88f, 1.78f, 140.85f, 0f, 144.2f, 0f)
                close()
                moveTo(5.87f, 0f)
                curveTo(8.74f, 0f, 10.99f, 1.96f, 11.52f, 4.65f)
                lineTo(12.94f, 2.64f)
                curveTo(14.08f, 1f, 15.94f, 0.05f, 17.79f, 0.05f)
                lineTo(18.09f, 0.05f)
                curveTo(21.14f, 0.21f, 23.67f, 2.75f, 23.67f, 6.02f)
                curveTo(23.67f, 7.3f, 23.31f, 8.57f, 22.56f, 9.62f)
                lineTo(19.74f, 13.68f)
                lineTo(22.56f, 17.74f)
                curveTo(23.31f, 18.83f, 23.67f, 20.07f, 23.67f, 21.34f)
                curveTo(23.67f, 24.72f, 20.97f, 27.54f, 17.62f, 27.5f)
                curveTo(15.76f, 27.5f, 13.86f, 26.59f, 12.72f, 24.85f)
                lineTo(11.48f, 23.03f)
                curveTo(10.91f, 25.67f, 8.7f, 27.5f, 5.87f, 27.5f)
                curveTo(2.6f, 27.5f, 0f, 24.76f, 0f, 21.39f)
                verticalLineTo(6.11f)
                curveTo(0f, 2.74f, 2.56f, 0f, 5.87f, 0f)
                close()
                moveTo(71.01f, 0.01f)
                curveTo(73.76f, 0.12f, 75.89f, 2.04f, 76.41f, 4.65f)
                lineTo(77.78f, 2.64f)
                curveTo(78.93f, 1f, 80.79f, 0.05f, 82.65f, 0.05f)
                lineTo(82.94f, 0.05f)
                curveTo(86f, 0.21f, 88.53f, 2.75f, 88.53f, 6.02f)
                curveTo(88.53f, 7.3f, 88.17f, 8.57f, 87.42f, 9.62f)
                lineTo(84.59f, 13.68f)
                lineTo(87.42f, 17.74f)
                curveTo(88.17f, 18.83f, 88.53f, 20.07f, 88.53f, 21.34f)
                curveTo(88.53f, 24.72f, 85.83f, 27.54f, 82.51f, 27.5f)
                curveTo(80.65f, 27.5f, 78.75f, 26.59f, 77.6f, 24.85f)
                lineTo(76.36f, 23.03f)
                curveTo(75.79f, 25.67f, 73.58f, 27.5f, 70.75f, 27.5f)
                curveTo(67.47f, 27.5f, 64.86f, 24.76f, 64.86f, 21.39f)
                verticalLineTo(6.11f)
                curveTo(64.86f, 2.74f, 67.43f, 0f, 70.75f, 0f)
                lineTo(71.01f, 0.01f)
                close()
            }
        }.build()

        return checkNotNull(_KSLogo) { "_KSLogo must not be null" }
    }

@Suppress("ObjectPropertyName")
private var _KSLogo: ImageVector? = null
