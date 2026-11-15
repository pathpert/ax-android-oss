package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kickstarter.models.pushdata.Activity
import com.kickstarter.models.pushdata.GCM
import kotlinx.parcelize.Parcelize

/**
 * Network or service component for PushNotificationEnvelope.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
@Parcelize
@androidx.annotation.Keep
data class PushNotificationEnvelope(
    private val activity: Activity? = null,
    private val erroredPledge: ErroredPledge? = null,
    private val gcm: GCM = GCM.builder().build(),
    private val message: Message? = null,
    private val project: Project? = null,
    private val survey: Survey? = null,
    private val pledgeRedemption: PledgeRedemption? = null,
) : Parcelable {
    fun activity() = this.activity
    fun erroredPledge() = this.erroredPledge
    fun gcm() = this.gcm
    fun message() = this.message
    fun project() = this.project
    fun survey() = this.survey
    fun pledgeRedemption() = this.pledgeRedemption

    @Parcelize
    data class Builder(
        private var activity: Activity? = null,
        private var erroredPledge: ErroredPledge? = null,
        private var gcm: GCM = GCM.builder().build(),
        private var message: Message? = null,
        private var project: Project? = null,
        private var survey: Survey? = null,
        private var pledgeRedemption: PledgeRedemption? = null
    ) : Parcelable {
        fun activity(activity: Activity?) = apply { this.activity = activity }
        fun erroredPledge(erroredPledge: ErroredPledge?) = apply { this.erroredPledge = erroredPledge }
        fun gcm(gcm: GCM) = apply { this.gcm = gcm }
        fun message(message: Message?) = apply { this.message = message }
        fun project(project: Project?) = apply { this.project = project }
        fun survey(survey: Survey?) = apply { this.survey = survey }
        fun pledgeRedemption(pledgeRedemption: PledgeRedemption?) = apply { this.pledgeRedemption = pledgeRedemption }
        fun build() = PushNotificationEnvelope(
            activity = activity,
            erroredPledge = erroredPledge,
            gcm = gcm,
            message = message,
            project = project,
            survey = survey,
            pledgeRedemption = pledgeRedemption,
        )
    }

    fun toBuilder() = Builder(
        activity = activity,
        erroredPledge = erroredPledge,
        gcm = gcm,
        message = message,
        project = project,
        survey = survey,
        pledgeRedemption = pledgeRedemption
    )

    fun isErroredPledge() = erroredPledge() != null
    fun isFriendFollow() = activity() != null && activity()?.category() == com.kickstarter.models.Activity.CATEGORY_FOLLOW
    fun isMessage() = message() != null
    fun isProjectActivity() = activity()?.let { PROJECT_NOTIFICATION_CATEGORIES.contains(it.category()) } ?: false
    fun isProjectReminder() = project() != null
    fun isProjectUpdateActivity() = activity() != null && activity()?.category() == com.kickstarter.models.Activity.CATEGORY_UPDATE
    fun isSurvey() = survey() != null
    fun isPledgeRedemption() = pledgeRedemption() != null

    fun signature(): Int = gcm().alert().hashCode()

    @Parcelize
    data class ErroredPledge(private val projectId: Long = 0L) : Parcelable {
        fun projectId() = this.projectId
        @Parcelize
        data class Builder(private var projectId: Long = 0L) : Parcelable {
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun build() = ErroredPledge(projectId = projectId)
        }
        fun toBuilder() = Builder(projectId = projectId)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class Message(
        private val messageThreadId: Long = 0L,
        private val projectId: Long = 0L
    ) : Parcelable {
        fun messageThreadId() = this.messageThreadId
        fun projectId() = this.projectId
        @Parcelize
        data class Builder(
            private var messageThreadId: Long = 0L,
            private var projectId: Long = 0L,
        ) : Parcelable {
            fun messageThreadId(messageThreadId: Long) = apply { this.messageThreadId = messageThreadId }
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun build() = Message(messageThreadId = messageThreadId, projectId = projectId)
        }
        fun toBuilder() = Builder(messageThreadId = messageThreadId, projectId = projectId)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class Project(
        private val id: Long = 0L,
        private val photo: String = ""
    ) : Parcelable {
        fun id() = this.id
        fun photo() = this.photo
        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var photo: String = "",
        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun photo(photo: String) = apply { this.photo = photo }
            fun build() = Project(id = id, photo = photo)
        }
        fun toBuilder() = Builder(id = id, photo = photo)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class Survey(
        private val id: Long = 0L,
        private val projectId: Long = 0L,
        private val urls: Urls? = null
    ) : Parcelable {
        fun id() = this.id
        fun projectId() = this.projectId
        fun urls() = this.urls
        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var projectId: Long = 0L,
            private var urls: Urls? = null
        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun urls(urls: Urls?) = apply { urls?.let { this.urls = it } }
            fun build() = Survey(id = id, projectId = projectId, urls = urls)
        }
        fun toBuilder() = Builder(id = id, projectId = projectId, urls = urls)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class PledgeRedemption(
        private val id: Long = 0L,
        @SerializedName("pledge_manager_path")
        private val pledgeRedemptionPath: String? = null
    ) : Parcelable {
        fun id() = this.id
        fun pledgeRedemptionPath() = this.pledgeRedemptionPath
        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            @SerializedName("pledge_manager_path")
            private var pledgeRedemptionPath: String? = null
        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun pledgeRedemptionPath(path: String?) = apply { path?.let { this.pledgeRedemptionPath = it } }
            fun build() = PledgeRedemption(id = id, pledgeRedemptionPath = pledgeRedemptionPath)
        }
        fun toBuilder() = Builder(id = id, pledgeRedemptionPath = pledgeRedemptionPath)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class Urls(private val web: Web = Web.builder().build()) : Parcelable {
        fun web() = this.web
        @Parcelize
        data class Builder(private var web: Web = Web.builder().build()) : Parcelable {
            fun web(web: Web?) = apply { this.web = web ?: Web.builder().build() }
            fun build() = Urls(web = web)
        }
        fun toBuilder() = Builder(web = web)
        companion object {
            @JvmStatic fun builder() = Builder()
        }
    }

    @Parcelize
    data class Web(private val survey: String? = null) : Parcelable {
        fun survey() = this.survey
        @Parcelize
        data class Builder(private var survey: String? = null) : Parcelable {
            fun survey(survey: String?) = apply { this.survey = survey }
            fun build() = Web(survey = survey)
        }
        fun toBuilder() = Builder(survey = survey)
        companion object {
            @JvmStatic fun builder() = Builder()
        }
    }

    companion object {
        private val PROJECT_NOTIFICATION_CATEGORIES = listOf(
            com.kickstarter.models.Activity.CATEGORY_BACKING,
            com.kickstarter.models.Activity.CATEGORY_CANCELLATION,
            com.kickstarter.models.Activity.CATEGORY_FAILURE,
            com.kickstarter.models.Activity.CATEGORY_LAUNCH,
            com.kickstarter.models.Activity.CATEGORY_SUCCESS
        )
        @JvmStatic fun builder(): Builder = Builder()
    }
}
