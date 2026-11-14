package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable domain model representing a project taxonomy category or subcategory.
 *
 * ### Negative Constraints:
 * - Do not mutate fields directly; use [builder] or [toBuilder].
 * - Do not assume [parentId] is non-zero; root categories have `parentId == 0L`.
 */
@Parcelize
@androidx.annotation.Keep
class Category internal constructor(
    private val analyticsName: String,
    private val color: Int?,
    private val id: Long,
    private val name: String,
    private val parent: Category?,
    private val parentId: Long,
    private val parentName: String?,
    private val position: Int,
    private val projectsCount: Int,
    private val slug: String
) : Parcelable, Comparable<Category>, Relay {

    /** Identifier name used in analytics event tracking. */
    fun analyticsName() = this.analyticsName

    /** Brand color integer associated with the category. */
    fun color() = this.color

    /** Persistent database identifier (PID) of the category. */
    override fun id() = this.id

    /** Localized display name of the category. */
    fun name() = this.name

    /** Parent category domain entity if this is a subcategory, or null for root categories. */
    fun parent() = this.parent

    /** Persistent database identifier of the parent category, or 0L if root. */
    fun parentId() = this.parentId

    /** Localized display name of the parent category, or null if root. */
    fun parentName() = this.parentName

    /** Ordering position index for presentation in category pickers. */
    fun position() = this.position

    /** Total number of campaigns belonging to this category. */
    fun projectsCount() = this.projectsCount

    /** URL-safe slug identifying this category (e.g., "art", "games/tabletop-games"). */
    fun slug() = this.slug

    @Parcelize
    data class Builder(
        private var analyticsName: String = "",
        private var color: Int? = 0,
        private var id: Long = 0L,
        private var name: String = "",
        private var parent: Category? = null,
        private var parentId: Long = 0L,
        private var parentName: String? = "",
        private var position: Int = 0,
        private var projectsCount: Int = 0,
        private var slug: String = ""
    ) : Parcelable {
        fun analyticsName(analyticsName: String?) = apply { analyticsName?.let { this.analyticsName = it } }
        fun color(color: Int?) = apply { this.color = color }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun name(name: String?) = apply { this.name = name ?: "" }
        fun parent(parent: Category?) = apply { this.parent = parent }
        fun parentId(parentId: Long?) = apply { this.parentId = parentId ?: 0L }
        fun parentName(parentName: String?) = apply { this.parentName = parentName }
        fun position(position: Int?) = apply { this.position = position ?: 0 }
        fun projectsCount(projectsCount: Int?) = apply { this.projectsCount = projectsCount ?: 0 }
        fun slug(slug: String?) = apply { this.slug = slug ?: "" }
        fun build() = Category(
            analyticsName = analyticsName,
            color = color,
            id = id,
            name = name,
            parent = parent,
            parentId = parentId,
            parentName = parentName,
            position = position,
            projectsCount = projectsCount,
            slug = slug
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        analyticsName = analyticsName,
        color = color,
        id = id,
        name = name,
        parent = parent,
        parentId = parentId,
        parentName = parentName,
        position = position,
        projectsCount = projectsCount,
        slug = slug
    )

    override fun compareTo(other: Category): Int {
        if (id() == other.id()) {
            return 0
        }
        if (isRoot && id() == other.rootId()) {
            return -1
        } else if (!isRoot && rootId() == other.id()) {
            return 1
        }
        return other.root()?.let { root()?.name()?.compareTo(it.name()) } ?: 0
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Category) {
            equals = id() == other.id() &&
                analyticsName() == other.analyticsName() &&
                color() == other.color() &&
                parentName() == other.parentName() &&
                parent() == other.parent() &&
                name() == other.name() &&
                projectsCount() == other.projectsCount() &&
                position() == other.position() &&
                parentId() == other.parentId() &&
                slug() == other.slug()
        }
        return equals
    }

    val isRoot: Boolean
        get() = parentId() == 0L

    fun root(): Category? {
        return if (isRoot) this else parent()
    }

    fun rootId(): Long {
        return if (isRoot) id() else parentId()
    }

    override fun hashCode(): Int {
        var result = id().hashCode()
        result = 31 * result + analyticsName().hashCode()
        result = 31 * result + (color()?.hashCode() ?: 0)
        result = 31 * result + (parentName()?.hashCode() ?: 0)
        result = 31 * result + (parent()?.hashCode() ?: 0)
        result = 31 * result + name().hashCode()
        result = 31 * result + projectsCount()
        result = 31 * result + position()
        result = 31 * result + parentId().hashCode()
        result = 31 * result + slug().hashCode()
        return result
    }
}
