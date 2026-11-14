package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

/**
 * Immutable domain model representing a project comment or threaded reply.
 *
 * ### Negative Constraints:
 * - Do not pass blank [body] text when creating new comments.
 */
@Parcelize
@androidx.annotation.Keep
class Comment private constructor(
    private val author: User,
    private val body: String,
    private val createdAt: DateTime?,
    private val deleted: Boolean,
    private val hasFlaggings: Boolean,
    private val sustained: Boolean,
    private val authorCanceledPledge: Boolean,
    private val cursor: String,
    private val repliesCount: Int,
    private val authorBadges: List<String>?,
    private val id: Long,
    private val parentId: Long
) : Parcelable, Relay {
    /** Domain model representing the user author who posted the comment. */
    fun author() = this.author

    /** Comment body text content. */
    fun body() = this.body

    /** Timestamp when the comment was published. */
    fun createdAt() = this.createdAt

    /** Whether the comment has been deleted by its author or moderator. */
    fun deleted() = this.deleted

    /** Whether the comment has been flagged for inappropriate content. */
    fun hasFlaggings() = this.hasFlaggings

    /** Whether the flagged content objection was sustained by moderators. */
    fun sustained() = this.sustained

    /** Whether the author had backed the project and subsequently canceled their pledge. */
    fun authorCanceledPledge() = this.authorCanceledPledge

    /** Pagination cursor for fetching threaded replies. */
    fun cursor() = this.cursor

    /** Total count of threaded child replies to this comment. */
    fun repliesCount() = this.repliesCount

    /** Creator, collaborator, or superbacker badge identifiers for the author. */
    fun authorBadges() = this.authorBadges

    /** Persistent database identifier (PID) of the comment. */
    override fun id() = this.id

    /** Identifier of parent comment if this is a reply, or -1 if a root comment. */
    fun parentId() = this.parentId

    @Parcelize
    data class Builder(
        private var author: User = User.builder().build(),
        private var body: String = "",
        private var createdAt: DateTime? = null,
        private var deleted: Boolean = false,
        private var hasFlaggings: Boolean = false,
        private var sustained: Boolean = false,
        private var authorCanceledPledge: Boolean = false,
        private var cursor: String = "",
        private var repliesCount: Int = 0,
        private var authorBadges: List<String>? = emptyList(),
        private var id: Long = -1,
        private var parentId: Long = -1
    ) : Parcelable {
        fun author(author: User?) = apply { author?.let { this.author = it } }
        fun cursor(cursor: String?) = apply { this.cursor = cursor ?: "" }
        fun authorBadges(authorBadges: List<String>?) = apply { this.authorBadges = authorBadges }
        fun repliesCount(repliesCount: Int?) = apply { this.repliesCount = repliesCount ?: 0 }
        fun body(body: String?) = apply { this.body = body ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { this.createdAt = createdAt }
        fun deleted(deleted: Boolean?) = apply { this.deleted = deleted ?: false }
        fun hasFlaggings(hasFlaggings: Boolean?) = apply { this.hasFlaggings = hasFlaggings ?: false }
        fun sustained(sustained: Boolean?) = apply { this.sustained = sustained ?: false }
        fun authorCanceledPledge(authorCanceledPledge: Boolean?) = apply { this.authorCanceledPledge = authorCanceledPledge ?: false }
        fun id(id: Long?) = apply { this.id = id ?: -1 }
        fun parentId(parentId: Long?) = apply { this.parentId = parentId ?: -1 }
        fun build() = Comment(
            author = author,
            cursor = cursor,
            authorBadges = authorBadges,
            repliesCount = repliesCount,
            body = body,
            createdAt = createdAt,
            deleted = deleted,
            hasFlaggings = hasFlaggings,
            sustained = sustained,
            authorCanceledPledge = authorCanceledPledge,
            id = id,
            parentId = parentId
        )
    }

    fun toBuilder() = Builder(
        author = author,
        cursor = cursor,
        authorBadges = authorBadges,
        repliesCount = repliesCount,
        body = body,
        createdAt = createdAt,
        deleted = deleted,
        hasFlaggings = hasFlaggings,
        sustained = sustained,
        authorCanceledPledge = authorCanceledPledge,
        id = id,
        parentId = parentId
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Comment) {
            equals = id() == other.id() &&
                body() == other.body() &&
                author() == other.author() &&
                authorBadges() == other.authorBadges() &&
                cursor() == other.cursor() &&
                deleted() == other.deleted() &&
                hasFlaggings() == other.hasFlaggings() &&
                sustained() == other.sustained() &&
                repliesCount() == other.repliesCount() &&
                authorCanceledPledge() == other.authorCanceledPledge() &&
                createdAt() == other.createdAt() &&
                parentId() == other.parentId()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
