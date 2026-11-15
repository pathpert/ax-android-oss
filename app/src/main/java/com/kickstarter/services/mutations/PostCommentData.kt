package com.kickstarter.services.mutations

import com.kickstarter.models.Comment

/**
 * Network or service component for PostCommentData.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
data class PostCommentData(
    val body: String,
    val parent: Comment?,
    val commentableId: String,
    val clientMutationId: String?
)
