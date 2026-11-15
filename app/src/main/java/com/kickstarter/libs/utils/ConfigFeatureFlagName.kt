package com.kickstarter.libs.utils

/**
 * Utility and helper extensions for ConfigFeatureFlagName.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
enum class ConfigFeatureFlagName(val featureFlag: String)
