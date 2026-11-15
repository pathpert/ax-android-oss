package com.kickstarter.libs

/**
 * Library utility component for Permission.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
enum class Permission(val permission: String) {
    COMMENT("comment"),
    EDIT_PROJECT("edit_project"),
    EDIT_FAQ("edit_faq"),
    FULFILLMENT("fulfillment"),
    POST("post"),
    VIEW_PLEDGES("view_pledges"),
    UNKNOWN("unknown")
}
