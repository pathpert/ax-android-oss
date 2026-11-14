package com.kickstarter.ui.compose.designsystem

// TODO: Change colors to reflect actual dark theme when available
val KSDarkCustomColors = KSCustomColors(
    // New Colors
    // Text
    textPrimary = grey_01,
    textInversePrimary = grey_10,
    textSecondary = grey_04,
    textInverseSecondary = grey_08,
    textDisabled = grey_06,
    textAccentGrey = grey_02,
    textAccentRed = red_05,
    textAccentRedBold = red_02,
    textAccentGreen = green_05,
    textAccentGreenBold = green_02,
    textAccentBlue = blue_05,
    textAccentBlueBold = blue_02,
    textAccentPurple = purple_05,
    textAccentPurpleBold = purple_02,
    textAccentYellow = yellow_05,
    textAccentYellowBold = yellow_02,

    purple_03 = purple_03,
    purple_08 = purple_08,
    yellow_03 = yellow_03,
    yellow_08 = yellow_08,

    // Background Colors
    backgroundSurfacePrimary = grey_10,
    backgroundSurfaceInverse = white,
    backgroundSurfaceSecondary = black,
    backgroundSurfaceRaised = grey_11,
    backgroundDisabled = grey_06,
    backgroundInverse = grey_10,
    backgroundInverseHover = grey_09,
    backgroundInversePressed = grey_08,
    backgroundSelected = grey_01,
    backgroundAction = grey_01,
    backgroundActionHover = white,
    backgroundActionDisabled = grey_06,
    backgroundActionPressed = grey_02,
    backgroundAccentGreenBold = green_05,
    backgroundAccentGreenBrand = green_05,
    backgroundAccentGreenSubtle = green_09,
    backgroundAccentBlueBold = blue_05,
    backgroundAccentBlueSubtle = blue_09,
    backgroundAccentOrangeSubtle = orange_09,
    backgroundAccentPurpleSubtle = purple_09,
    backgroundDangerBold = red_05,
    backgroundDangerSubtle = red_10,
    backgroundDangerBoldPressed = red_03,
    backgroundDangerBoldHovered = red_04,
    backgroundDangerSubtleHovered = red_08,
    backgroundAccentGrayBold = grey_06,
    backgroundAccentGraySubtle = grey_09,
    backgroundWarningBold = yellow_05,
    backgroundWarningSubtle = yellow_10,
    backgroundScheduledTag = grey_07,

    // Border Colors
    borderAccentBlueBold = blue_03,
    borderAccentBlueSubtle = blue_08,
    borderAccentGreenSubtle = green_08,
    borderWarningBold = yellow_03,
    borderWarningSubtle = yellow_08,
    borderDisabled = grey_09,
    borderBoldHover = grey_03,
    borderSubtleHover = white,
    borderActive = grey_01,
    borderBold = grey_06,
    borderDangerBold = red_03,
    borderDangerSubtle = red_08,
    borderSubtle = grey_08,
    borderFocus = blue_05,

    // Icon Colors
    icon = grey_02,
    iconDanger = red_03,
    iconWarning = yellow_03,
    iconSubtle = grey_05,
    iconInverse = grey_08,
    iconInfo = blue_03,
    iconDisabled = grey_06,

    // Floating BottomNav Dark color schema
    navBackground = grey_900,
    navBackgroundHighlight = green_400,
    navIcon = grey_100,
    navIconSelected = grey_1000,
    navBackgroundTapped = green_800,
    navIconBorderAvatar = grey_900,
    navBoxShadow = black,

    // DiscoveryBanner semantic color naming
    discoveryBanner = KSDiscoveryBannerColors(
        background = purple_03,
        text = grey_10,
        buttonBackground = white,
        buttonText = grey_10,
    ),

    // VideoPlayer semantic color naming
    videoPlayer = KSVideoPlayerColors(
        content = white,
        background = grey_095,
        iconShadow = black.copy(alpha = 0.1f),
        buttonBorder = white,
        buttonText = white,
        progressBase = white,
        progressComplete = green_300,
        progressTrack = grey_055,
    ),

    // Social Share semantic color naming
    socialShare = KSSocialShareColors(
        background = kds_create_500,
        dragHandle = black.copy(alpha = 0.3f),
        text = black,
        platformLabel = black,
        iconBackground = kds_support_700,
        monochromeIconTint = white
    ),

    preLaunchProjectPage = PreLaunchProjectPageColors(
        background = kds_support_700
    ),

    // OLD COLORS
    // Greens
    kds_create_100 = kds_create_700,
    kds_create_300 = kds_create_500,
    kds_create_500 = kds_create_300,
    kds_create_700 = kds_create_100,

    // BLUES
    kds_trust_100 = kds_trust_700,
    kds_trust_300 = kds_trust_500,
    kds_trust_500 = kds_trust_300,
    kds_trust_700 = kds_trust_100,

    // CORALS
    kds_celebrate_100 = kds_celebrate_700,
    kds_celebrate_300 = kds_celebrate_500,
    kds_celebrate_500 = kds_celebrate_300,
    kds_celebrate_700 = kds_celebrate_100,

    // GREYS
    kds_white = kds_black,
    kds_support_100 = kds_support_700,
    kds_support_200 = kds_support_500,
    kds_support_300 = kds_support_400,
    kds_support_400 = kds_support_300,
    kds_support_500 = kds_support_200,
    kds_support_700 = kds_support_100,
    kds_black = kds_white,

    // FUNCTIONAL COLORS
    kds_alert = kds_celebrate_500,
    kds_warn = kds_warn,
    kds_inform = kds_inform,
    facebook_blue = facebook_blue,
    red_light = red_light,
    red_03 = red_03,
    red_07 = red_07,
    grey_03 = grey_03,
    grey_05 = grey_05,
    grey_10 = grey_10,
    blue_03 = blue_03,
    blue_09 = blue_09,
    green_02 = green_02,
    green_07 = green_07,
    green_06 = green_06,
)
