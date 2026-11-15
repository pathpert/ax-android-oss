package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Extracted into its own data class to work around a JVM limitation:
// Kotlin data classes with many defaulted parameters generate synthetic constructors
// that can exceed the 255-parameter JVM limit (ClassFormatError: Too many arguments).
// Grouping component-specific colors reduces the parameter count on KSCustomColors.
/**
 * UI component rendering KSDiscoveryBannerColors.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Immutable
data class KSDiscoveryBannerColors(
    val background: Color = Color.Unspecified,
    val text: Color = Color.Unspecified,
    val buttonBackground: Color = Color.Unspecified,
    val buttonText: Color = Color.Unspecified,
)

@Immutable
data class KSSocialShareColors(
    val background: Color = Color.Unspecified,
    val dragHandle: Color = Color.Unspecified,
    // Text sits on the green background — always black regardless of theme.
    val text: Color = Color.Unspecified,
    val platformLabel: Color = Color.Unspecified,
    val iconBackground: Color = Color.Unspecified,
    val monochromeIconTint: Color = Color.Unspecified
)

// Extracted into its own data class to work around a JVM limitation:
// Kotlin data classes with many defaulted parameters generate synthetic constructors
// that can exceed the 255-parameter JVM limit (ClassFormatError: Too many arguments).
// Grouping component-specific colors reduces the parameter count on KSCustomColors.
@Immutable
data class KSVideoPlayerColors(
    val content: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val iconShadow: Color = Color.Unspecified,
    val buttonBorder: Color = Color.Unspecified,
    val buttonText: Color = Color.Unspecified,
    val progressBase: Color = Color.Unspecified,
    val progressComplete: Color = Color.Unspecified,
    val progressTrack: Color = Color.Unspecified,
)

@Immutable
data class PreLaunchProjectPageColors(
    val background: Color = Color.Unspecified,
)

@Immutable
data class KSCustomColors(
    // NEW COLORS
    // Text Colors
    val textPrimary: Color = Color.Unspecified,
    val textInversePrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textInverseSecondary: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val textAccentGrey: Color = Color.Unspecified,
    val textAccentRed: Color = Color.Unspecified,
    val textAccentRedBold: Color = Color.Unspecified,
    val textAccentGreen: Color = Color.Unspecified,
    val textAccentGreenBold: Color = Color.Unspecified,
    val textAccentBlue: Color = Color.Unspecified,
    val textAccentBlueBold: Color = Color.Unspecified,
    val textAccentPurple: Color = Color.Unspecified,
    val textAccentPurpleBold: Color = Color.Unspecified,
    val textAccentYellow: Color = Color.Unspecified,
    val textAccentYellowBold: Color = Color.Unspecified,

    val purple_03: Color = Color.Unspecified,
    val purple_08: Color = Color.Unspecified,
    val yellow_03: Color = Color.Unspecified,
    val yellow_08: Color = Color.Unspecified,

    // Background Colors
    val backgroundSurfacePrimary: Color = Color.Unspecified,
    val backgroundSurfaceInverse: Color = Color.Unspecified,
    val backgroundSurfaceSecondary: Color = Color.Unspecified,
    val backgroundSurfaceRaised: Color = Color.Unspecified,
    val backgroundDisabled: Color = Color.Unspecified,
    val backgroundInverse: Color = Color.Unspecified,
    val backgroundInverseHover: Color = Color.Unspecified,
    val backgroundInversePressed: Color = Color.Unspecified,
    val backgroundSelected: Color = Color.Unspecified,
    val backgroundAction: Color = Color.Unspecified,
    val backgroundActionHover: Color = Color.Unspecified,
    val backgroundActionDisabled: Color = Color.Unspecified,
    val backgroundActionPressed: Color = Color.Unspecified,
    val backgroundAccentGreenBold: Color = Color.Unspecified,
    val backgroundAccentGreenBrand: Color = Color.Unspecified,
    val backgroundAccentGreenSubtle: Color = Color.Unspecified,
    val backgroundAccentBlueBold: Color = Color.Unspecified,
    val backgroundAccentBlueSubtle: Color = Color.Unspecified,
    val backgroundAccentOrangeSubtle: Color = Color.Unspecified,
    val backgroundAccentPurpleSubtle: Color = Color.Unspecified,
    val backgroundDangerBold: Color = Color.Unspecified,
    val backgroundDangerSubtle: Color = Color.Unspecified,
    val backgroundDangerBoldPressed: Color = Color.Unspecified,
    val backgroundDangerBoldHovered: Color = Color.Unspecified,
    val backgroundDangerSubtleHovered: Color = Color.Unspecified,
    val backgroundAccentGrayBold: Color = Color.Unspecified,
    val backgroundAccentGraySubtle: Color = Color.Unspecified,
    val backgroundWarningBold: Color = Color.Unspecified,
    val backgroundWarningSubtle: Color = Color.Unspecified,
    val backgroundScheduledTag: Color = Color.Unspecified,

    // Border Colors
    val borderAccentBlueBold: Color = Color.Unspecified,
    val borderAccentBlueSubtle: Color = Color.Unspecified,
    val borderAccentGreenSubtle: Color = Color.Unspecified,
    val borderWarningBold: Color = Color.Unspecified,
    val borderWarningSubtle: Color = Color.Unspecified,
    val borderDisabled: Color = Color.Unspecified,
    val borderBoldHover: Color = Color.Unspecified,
    val borderSubtleHover: Color = Color.Unspecified,
    val borderActive: Color = Color.Unspecified,
    val borderBold: Color = Color.Unspecified,
    val borderDangerBold: Color = Color.Unspecified,
    val borderDangerSubtle: Color = Color.Unspecified,
    val borderSubtle: Color = Color.Unspecified,
    val borderFocus: Color = Color.Unspecified,

    // Icon Colors
    val icon: Color = Color.Unspecified,
    val iconDanger: Color = Color.Unspecified,
    val iconWarning: Color = Color.Unspecified,
    val iconSubtle: Color = Color.Unspecified,
    val iconInverse: Color = Color.Unspecified,
    val iconInfo: Color = Color.Unspecified,
    val iconDisabled: Color = Color.Unspecified,

    // BottomNav semantic color naming
    val navBackground: Color = Color.Unspecified,
    val navIcon: Color = Color.Unspecified,
    val navIconSelected: Color = Color.Unspecified,
    val navBackgroundHighlight: Color = Color.Unspecified,
    val navBackgroundTapped: Color = Color.Unspecified,
    val navIconBorderAvatar: Color = Color.Unspecified,
    val navBoxShadow: Color = Color.Unspecified,

    // Component semantic colors
    val discoveryBanner: KSDiscoveryBannerColors = KSDiscoveryBannerColors(),
    val videoPlayer: KSVideoPlayerColors = KSVideoPlayerColors(),
    val socialShare: KSSocialShareColors = KSSocialShareColors(),
    val preLaunchProjectPage: PreLaunchProjectPageColors = PreLaunchProjectPageColors(),

    // OLD COLORS
    // Greens
    val kds_create_100: Color = Color.Unspecified,
    val kds_create_300: Color = Color.Unspecified,
    val kds_create_500: Color = Color.Unspecified,
    val kds_create_700: Color = Color.Unspecified,

    // BLUES
    val kds_trust_100: Color = Color.Unspecified,
    val kds_trust_300: Color = Color.Unspecified,
    val kds_trust_500: Color = Color.Unspecified,
    val kds_trust_700: Color = Color.Unspecified,

    // CORALS
    val kds_celebrate_100: Color = Color.Unspecified,
    val kds_celebrate_300: Color = Color.Unspecified,
    val kds_celebrate_500: Color = Color.Unspecified,
    val kds_celebrate_700: Color = Color.Unspecified,

    // GREYS
    val kds_white: Color = Color.Unspecified,
    val kds_support_100: Color = Color.Unspecified,
    val kds_support_200: Color = Color.Unspecified,
    val kds_support_300: Color = Color.Unspecified,
    val kds_support_400: Color = Color.Unspecified,
    val kds_support_500: Color = Color.Unspecified,
    val kds_support_700: Color = Color.Unspecified,
    val kds_black: Color = Color.Unspecified,

    // FUNCTIONAL COLORS
    val kds_alert: Color = Color.Unspecified,
    val kds_warn: Color = Color.Unspecified,
    val kds_inform: Color = Color.Unspecified,
    val facebook_blue: Color = Color.Unspecified,
    val red_light: Color = Color.Unspecified,
    val red_03: Color = Color.Unspecified,
    val red_07: Color = Color.Unspecified,
    val grey_03: Color = Color.Unspecified,
    val grey_05: Color = Color.Unspecified,
    val grey_10: Color = Color.Unspecified,
    val blue_03: Color = Color.Unspecified,
    val blue_09: Color = Color.Unspecified,
    val green_02: Color = Color.Unspecified,
    val green_07: Color = Color.Unspecified,
    val green_06: Color = Color.Unspecified,
)

val LocalKSCustomColors = staticCompositionLocalOf {
    KSCustomColors()
}
