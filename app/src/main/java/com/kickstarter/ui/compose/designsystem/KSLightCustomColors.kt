package com.kickstarter.ui.compose.designsystem

val KSLightCustomColors = KSCustomColors(
    // NEW COLORS    // Text
    textPrimary = grey_10,
    textInversePrimary = white,
    textSecondary = grey_07,
    textInverseSecondary = grey_03,
    textDisabled = grey_05,
    textAccentGrey = grey_10,
    textAccentRed = red_06,
    textAccentRedBold = red_08,
    textAccentGreen = green_06,
    textAccentGreenBold = green_08,
    textAccentBlue = blue_06,
    textAccentBlueBold = blue_08,
    textAccentPurple = purple_06,
    textAccentPurpleBold = purple_08,
    textAccentYellow = yellow_06,
    textAccentYellowBold = yellow_08,

    purple_03 = purple_03,
    purple_08 = purple_08,
    yellow_03 = yellow_03,
    yellow_08 = yellow_08,

    // Background Colors
    backgroundSurfacePrimary = white,
    backgroundSurfaceInverse = grey_10,
    backgroundSurfaceSecondary = grey_01,
    backgroundSurfaceRaised = white,
    backgroundDisabled = grey_04,
    backgroundInverse = white,
    backgroundInverseHover = grey_02,
    backgroundInversePressed = grey_03,
    backgroundSelected = grey_09,
    backgroundAction = grey_10,
    backgroundActionHover = black,
    backgroundActionDisabled = grey_05,
    backgroundActionPressed = grey_09,
    backgroundAccentGreenBold = green_06,
    backgroundAccentGreenBrand = green_05,
    backgroundAccentGreenSubtle = green_02,
    backgroundAccentBlueBold = blue_06,
    backgroundAccentBlueSubtle = blue_02,
    backgroundAccentOrangeSubtle = orange_02,
    backgroundAccentPurpleSubtle = purple_02,
    backgroundDangerBold = red_06,
    backgroundDangerSubtle = red_02,
    backgroundDangerBoldPressed = red_08,
    backgroundDangerBoldHovered = red_07,
    backgroundDangerSubtleHovered = red_03,
    backgroundAccentGrayBold = grey_06,
    backgroundAccentGraySubtle = grey_02,
    backgroundWarningBold = yellow_06,
    backgroundWarningSubtle = yellow_02,
    backgroundScheduledTag = grey_04,

    // Border Colors
    borderAccentBlueBold = blue_08,
    borderAccentBlueSubtle = blue_04,
    borderAccentGreenSubtle = green_04,
    borderWarningBold = yellow_08,
    borderWarningSubtle = yellow_04,
    borderDisabled = grey_02,
    borderBoldHover = grey_08,
    borderSubtleHover = grey_05,
    borderActive = grey_08,
    borderBold = grey_04,
    borderDangerBold = red_08,
    borderDangerSubtle = red_04,
    borderSubtle = grey_03,
    borderFocus = blue_05,

    // Icon Colors
    icon = grey_08,
    iconDanger = red_07,
    iconWarning = yellow_07,
    iconSubtle = grey_06,
    iconInverse = grey_03,
    iconInfo = blue_07,
    iconDisabled = grey_05,

    // Floating BottomNav light color schema
    navBackground = white,
    navBackgroundHighlight = green_200,
    navIcon = grey_800,
    navIconSelected = grey_800,
    navBackgroundTapped = green_100,
    navIconBorderAvatar = white,
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
        iconBackground = kds_white,
        monochromeIconTint = black
    ),

    preLaunchProjectPage = PreLaunchProjectPageColors(
        background = kds_white
    ),

    // OLD COLORS
    // Greens
    kds_create_100 = kds_create_100,
    kds_create_300 = kds_create_300,
    kds_create_500 = kds_create_500,
    kds_create_700 = kds_create_700,

    // BLUES
    kds_trust_100 = kds_trust_100,
    kds_trust_300 = kds_trust_300,
    kds_trust_500 = kds_trust_500,
    kds_trust_700 = kds_trust_700,

    // CORALS
    kds_celebrate_100 = kds_celebrate_100,
    kds_celebrate_300 = kds_celebrate_300,
    kds_celebrate_500 = kds_celebrate_500,
    kds_celebrate_700 = kds_celebrate_700,

    // GREYS
    kds_white = kds_white,
    kds_support_100 = kds_support_100,
    kds_support_200 = kds_support_200,
    kds_support_300 = kds_support_300,
    kds_support_400 = kds_support_400,
    kds_support_500 = kds_support_500,
    kds_support_700 = kds_support_700,
    kds_black = kds_black,

    // FUNCTIONAL COLORS
    kds_alert = kds_alert,
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
