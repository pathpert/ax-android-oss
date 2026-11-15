package com.kickstarter.libs.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility and helper extensions for fromJson.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
internal inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)
