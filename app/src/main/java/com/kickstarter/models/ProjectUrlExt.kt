package com.kickstarter.models

import android.net.Uri

/**
 * Extension methods for computing canonical URLs on [Project].
 */

/** Web URL for the creator biography page. */
fun Project.creatorBioUrl(): String = urls().web().creatorBio()

/** Web URL for the campaign description. */
fun Project.descriptionUrl(): String = urls().web().description()

/** Web URL for the campaign updates feed. Returns empty string if unavailable. */
fun Project.updatesUrl(): String = urls().web().updates() ?: ""

/** Canonical public web URL for this project. */
fun Project.webProjectUrl(): String = urls().web().project()

/** Canonical HTTPS web project URL. */
fun Project.secureWebProjectUrl(): String =
    Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString()

/** Web URL for creating a new pledge on this project. */
fun Project.newPledgeUrl(): String =
    Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new").toString()

/** Web URL for modifying an existing pledge on this project. */
fun Project.editPledgeUrl(): String =
    Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit").toString()

