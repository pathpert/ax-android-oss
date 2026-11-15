package com.kickstarter.models

import android.content.res.Resources
import android.os.Parcelable
import com.kickstarter.R
import kotlinx.parcelize.Parcelize

/**
 * Immutable domain model representing a registered Kickstarter user or creator.
 *
 * Negative constraints:
 * - Do not assume [email] is present for anonymous or public user cards.
 * - Do not confuse the integer [id] with GraphQL Relay global IDs.
 */
@Deprecated("Legacy monolithic domain model. For new code, consume feature-scoped contracts in com.kickstarter.features.*")
@Parcelize
@androidx.annotation.Keep
class User internal constructor(
    private val alumniNewsletter: Boolean,
    private val artsCultureNewsletter: Boolean,
    private val avatar: Avatar,
    private val backedProjectsCount: Int,
    private val backingActionCount: Int,
    private val createdProjectsCount: Int,
    private val draftProjectsCount: Int,
    private val erroredBackingsCount: Int,
    private val facebookConnected: Boolean,
    private val filmNewsletter: Boolean,
    private val gamesNewsletter: Boolean,
    private val happeningNewsletter: Boolean,
    private val id: Long,
    private val inventNewsletter: Boolean,
    private val isAdmin: Boolean,
    private val isEmailVerified: Boolean,
    private val email: String?,
    private val hasPassword: Boolean?,
    private val isCreator: Boolean?,
    private val isDeliverable: Boolean?,
    private val chosenCurrency: String?,
    private val location: Location?,
    private val memberProjectsCount: Int,
    private val musicNewsletter: Boolean,
    private val name: String,
    private val notifyMobileOfBackings: Boolean,
    private val notifyMobileOfComments: Boolean,
    private val notifyMobileOfCreatorEdu: Boolean,
    private val notifyMobileOfFollower: Boolean,
    private val notifyMobileOfFriendActivity: Boolean,
    private val notifyMobileOfMessages: Boolean,
    private val notifyMobileOfPostLikes: Boolean,
    private val notifyMobileOfUpdates: Boolean,
    private val notifyMobileOfMarketingUpdate: Boolean,
    private val notifyOfBackings: Boolean,
    private val notifyOfComments: Boolean,
    private val notifyOfCommentReplies: Boolean,
    private val notifyOfCreatorDigest: Boolean,
    private val notifyOfCreatorEdu: Boolean,
    private val notifyOfFollower: Boolean,
    private val notifyOfFriendActivity: Boolean,
    private val notifyOfMessages: Boolean,
    private val notifyOfUpdates: Boolean,
    private val optedOutOfRecommendations: Boolean,
    private val ppoHasAction: Boolean,
    private val promoNewsletter: Boolean,
    private val publishingNewsletter: Boolean,
    private val showPublicProfile: Boolean,
    private val needsPassword: Boolean?,
    private val social: Boolean,
    private val starredProjectsCount: Int,
    private val unreadMessagesCount: Int,
    private val unseenActivityCount: Int,
    private val weeklyNewsletter: Boolean
) : Parcelable, Relay {

    fun alumniNewsletter() = this.alumniNewsletter
    fun artsCultureNewsletter() = this.artsCultureNewsletter
    fun avatar() = this.avatar
    fun backedProjectsCount() = this.backedProjectsCount
    fun backingActionCount() = this.backingActionCount
    fun createdProjectsCount() = this.createdProjectsCount
    fun draftProjectsCount() = this.draftProjectsCount
    fun erroredBackingsCount() = this.erroredBackingsCount
    fun facebookConnected() = this.facebookConnected
    fun filmNewsletter() = this.filmNewsletter
    fun gamesNewsletter() = this.gamesNewsletter
    fun happeningNewsletter() = this.happeningNewsletter
    override fun id() = this.id
    fun isAdmin() = this.isAdmin
    fun hasPassword() = this.hasPassword
    fun isEmailVerified() = this.isEmailVerified
    fun isCreator() = this.isCreator
    fun isDeliverable() = this.isDeliverable
    fun email() = this.email
    fun chosenCurrency() = this.chosenCurrency
    fun inventNewsletter() = this.inventNewsletter
    fun location() = this.location
    fun memberProjectsCount() = this.memberProjectsCount
    fun musicNewsletter() = this.musicNewsletter
    fun name() = this.name
    fun notifyMobileOfBackings() = this.notifyMobileOfBackings
    fun notifyMobileOfComments() = this.notifyMobileOfComments
    fun notifyMobileOfCreatorEdu() = this.notifyMobileOfCreatorEdu
    fun notifyMobileOfFollower() = this.notifyMobileOfFollower
    fun notifyMobileOfFriendActivity() = this.notifyMobileOfFriendActivity
    fun notifyMobileOfMessages() = this.notifyMobileOfMessages
    fun notifyMobileOfPostLikes() = this.notifyMobileOfPostLikes
    fun notifyMobileOfUpdates() = this.notifyMobileOfUpdates
    fun notifyMobileOfMarketingUpdate() = this.notifyMobileOfMarketingUpdate
    fun notifyOfBackings() = this.notifyOfBackings
    fun notifyOfComments() = this.notifyOfComments
    fun notifyOfCommentReplies() = this.notifyOfCommentReplies
    fun notifyOfCreatorDigest() = this.notifyOfCreatorDigest
    fun notifyOfCreatorEdu() = this.notifyOfCreatorEdu
    fun notifyOfFollower() = this.notifyOfFollower
    fun notifyOfFriendActivity() = this.notifyOfFriendActivity
    fun notifyOfMessages() = this.notifyOfMessages
    fun notifyOfUpdates() = this.notifyOfUpdates
    fun optedOutOfRecommendations() = this.optedOutOfRecommendations
    fun ppoHasAction() = this.ppoHasAction
    fun promoNewsletter() = this.promoNewsletter
    fun publishingNewsletter() = this.publishingNewsletter
    fun showPublicProfile() = this.showPublicProfile
    fun needsPassword() = this.needsPassword
    fun social() = this.social
    fun starredProjectsCount() = this.starredProjectsCount
    fun unreadMessagesCount() = this.unreadMessagesCount
    fun unseenActivityCount() = this.unseenActivityCount
    fun weeklyNewsletter() = this.weeklyNewsletter

    class Builder : BaseUserBuilder<Builder>()

    fun param(): String = id().toString()

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder(): Builder = UserBuilderHelper.toBuilder(this)

    enum class EmailFrequency(private val stringResId: Int) {
        TWICE_A_DAY_SUMMARY(R.string.Twice_a_day_summary), DAILY_SUMMARY(R.string.Daily_summary);

        companion object {
            fun getStrings(resources: Resources): Array<String?> {
                val strings = arrayOfNulls<String>(values().size)
                val values = values()
                var i = 0
                val valuesLength = values.size
                while (i < valuesLength) {
                    val emailFrequency = values[i]
                    strings[i] = resources.getString(emailFrequency.stringResId)
                    i++
                }
                return strings
            }
        }
    }

    override fun equals(obj: Any?): Boolean = UserEquality.areEqual(this, obj)

    override fun hashCode(): Int = super.hashCode()
}
