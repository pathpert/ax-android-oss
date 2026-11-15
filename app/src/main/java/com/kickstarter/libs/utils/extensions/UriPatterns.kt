package com.kickstarter.libs.utils.extensions

import java.util.regex.Pattern

internal const val VERIFICATION = "/profile/verify_email"
internal const val KSDOMAIN = "kickstarter.com"

/**
 * Utility and helper extensions for EMAIL_DOMAINS.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
internal val EMAIL_DOMAINS = Pattern.compile("\\A(?:me|ea|clicks|click|emails|email|e2|e3)\\.kickstarter\\.com\\z")

// /projects/:creator_param/:project_param/checkouts/1/thanks
internal val CHECKOUT_THANKS_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z"
)

// /discover
// /discover/.*
internal val DISCOVER_PATTERN = Pattern.compile("\\A\\/discover(?:\\/.*)?\\z")

// /discover/categories/param
internal val DISCOVER_CATEGORIES_PATTERN = Pattern.compile("\\A\\/discover\\/categories\\/.*")

// /discover/param
internal val DISCOVER_SCOPE_PATTERN = Pattern.compile("\\A\\/discover\\/([a-zA-Z0-9-_]+)\\z")

// /discover/places/param
internal val DISCOVER_PLACES_PATTERN =
    Pattern.compile("\\A\\/discover\\/places\\/[a-zA-Z0-9-_]+\\z")

// /discover/advanced?sort=param
internal val DISCOVER_SORT_PATTERN = Pattern.compile("\\A\\/discover\\/advanced.*")

//  /projects/:creator_param/:project_param/pledge
internal val NATIVE_CHECKOUT_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/pledge\\z"
)

// /checkouts/:checkout_id/guest/new
internal val NEW_GUEST_CHECKOUT_PATTERN = Pattern.compile(
    "\\A\\/checkouts\\/[a-zA-Z0-9-_]+\\/guest\\/new\\z"
)

// /projects/:creator_param/:project_param
internal val PROJECT_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/?\\z"
)

//  /projects/:creator_param/:project_param/surveys/:survey_param
internal val PROJECT_SURVEY = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/surveys\\/[a-zA-Z0-9-_]+\\z"
)

//  /projects/:creator_param/:project_param/surveys/:survey_param/edit
internal val PROJECT_SURVEY_EDIT = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/surveys\\/[a-zA-Z0-9-_]+\\/edit\\z"
)

//  /projects/:creator_param/:project_param/surveys/:survey_param/edit_address
internal val PROJECT_SURVEY_EDIT_ADDRESS = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/surveys\\/[a-zA-Z0-9-_]+\\/edit_address\\z"
)

//  /projects/:creator_param/:project_param/backing/survey_responses
internal val PROJECT_SURVEY_RESPONSE = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/backing\\/survey_responses\\z"
)

//  /projects/:creator_param/:project_param/backing/pledge_redemption
internal val PROJECT_SURVEY_BACKING_PLEDGE_REDEMPTION = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/backing\\/pledge_redemption\\z"
)

//  /projects/:creator_param/:project_param/backing/redeem
internal val PROJECT_BACKING_REDEEM = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/backing\\/redeem\\z"
)

//  /projects/:creator_param/:project_param/mark_reward_fulfilled/true
internal val PROJECT_REWARD_FULFILLMENT = Pattern.compile(
    "\\A/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/mark_reward_fulfilled/true+\\z"
)

// /projects/:creator_param/:project_param/comments
internal val PROJECT_COMMENTS_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/comments\\z"
)

// save=true|false
internal val PROJECT_SAVE_QUERY_PATTERN = Pattern.compile(
    "save(\\=[a-zA-Z]+)"
)

// /projects/:creator_param/:project_param/posts/:update_param/comments
internal val PROJECT_UPDATE_COMMENTS_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\/[a-zA-Z0-9-_]+\\/comments\\z"
)

// /projects/:creator_param/:project_param/posts/:update_param
internal val PROJECT_UPDATE_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\/[a-zA-Z0-9-_]+\\z"
)

// /projects/:creator_param/:project_param/posts
internal val PROJECT_UPDATES_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\z"
)

// /users/:user_param/surveys/:survey_response_id": userSurvey
internal val USER_SURVEY = Pattern.compile(
    "\\A\\/users(\\/[a-zA-Z0-9_-]+)?\\/surveys\\/[a-zA-Z0-9-_]+\\z"
)

// /video-feed
internal val VIDEO_FEED_PATTERN = Pattern.compile(
    "\\A\\/video-feed\\/?\\z"
)

internal val MAIN_PAGE_OPEN_BUTTON_QUERYPARAMS = Pattern.compile(
    "\\Aapp_banner=1&ref=nav\\z"
)

//  /projects/:creator_param/:project_param/backing/details
internal val PROJECT_BACKING_DETAILS_URL = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/backing\\/details\\z"
)

//  /projects/:creator_param/:project_param/order_edits/:order_edit_id/checkout
internal val PM_ORDER_EDIT_CHECKOUT_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/order_edits\\/[a-zA-Z0-9_-]+\\/checkout\\z"
)

