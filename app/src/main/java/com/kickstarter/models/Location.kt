package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable domain model representing a geographic location for project discovery or shipping.
 *
 * ### Negative Constraints:
 * - Do not mutate fields directly; use [builder] or [toBuilder].
 * - Do not assume [state] is populated for non-US locations.
 */
@Parcelize
class Location private constructor(
    private val id: Long,
    private val city: String,
    private val country: String,
    private val displayableName: String,
    private val expandedCountry: String,
    private val name: String,
    private val projectsCount: Int,
    private val state: String
) : Parcelable, Relay {

    /** Formatted full location string suitable for UI display (e.g., "Brooklyn, NY"). */
    fun displayableName() = this.displayableName

    /** City name component. */
    fun city() = this.city

    /** ISO 3166-1 alpha-2 two-letter country code. */
    fun country() = this.country

    /** Full expanded country name (e.g., "United States"). */
    fun expandedCountry() = this.expandedCountry

    /** Primary location name. */
    fun name() = this.name

    /** State or province subdivision component. */
    fun state() = this.state

    /** Number of active projects originating from this location. */
    fun projectsCount() = this.projectsCount

    /** Persistent database identifier (PID) of the location. */
    override fun id(): Long = this.id

    @Parcelize
    data class Builder(
        private var id: Long = 0L,
        private var city: String = "",
        private var country: String = "",
        private var displayableName: String = "",
        private var expandedCountry: String = "",
        private var name: String = "",
        private var projectsCount: Int = 0,
        private var state: String = ""
    ) : Parcelable {
        fun displayableName(dName: String?) = apply { dName?.let { this.displayableName = it } }
        fun city(city: String?) = apply { city?.let { this.city = it } }
        fun country(count: String?) = apply { count?.let { this.country = it } }
        fun expandedCountry(expCount: String?) = apply { expCount?.let { this.expandedCountry = it } }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun state(state: String?) = apply { state?.let { this.state = it } }
        fun projectsCount(pCount: Int?) = apply { pCount?.let { this.projectsCount = it } }
        fun id(id: Long?) = apply { id?.let { this.id = it } }
        fun build() = Location(
            id = id,
            city = city,
            country = country,
            displayableName = displayableName,
            expandedCountry = expandedCountry,
            name = name,
            projectsCount = projectsCount,
            state = state
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        id = id,
        city = city,
        country = country,
        displayableName = displayableName,
        expandedCountry = expandedCountry,
        name = name,
        projectsCount = projectsCount,
        state = state
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Location) {
            equals = id() == other.id() &&
                city() == other.city() &&
                country() == other.country() &&
                displayableName() == other.displayableName() &&
                expandedCountry() == other.expandedCountry() &&
                name() == other.name() &&
                projectsCount() == other.projectsCount() &&
                state() == other.state()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
