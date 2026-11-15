package com.kickstarter.ui.activities

import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNotificationsBinding
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.User
import com.kickstarter.viewmodels.NotificationsViewModel

internal fun NotificationsActivity.displayPreferences(
    user: User,
    binding: ActivityNotificationsBinding,
    viewModel: NotificationsViewModel.NotificationsViewModel
) {
    binding.projectNotificationsCount.text =
        user.backedProjectsCount().intValueOrZero().toString()

    displayMarketingUpdates(user, binding)
    displayBackingsNotificationSettings(user, binding, viewModel)
    displayCommentsNotificationSettings(user, binding)
    displayCommentRepliesNotificationSettings(user, binding)
    displayCreatorTipsNotificationSettings(user, binding)
    displayFollowerNotificationSettings(user, binding)
    displayFriendActivityNotificationSettings(user, binding)
    displayMessagesNotificationSettings(user, binding)
    displayPostLikesNotificationSettings(user, binding)
    displayUpdatesNotificationSettings(user, binding)
}

private fun NotificationsActivity.displayMarketingUpdates(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfMarketingUpdates = user.notifyMobileOfMarketingUpdate().isTrue()
    toggleImageButtonIconColor(
        binding.marketingUpdatesPhoneIcon,
        this.notifyMobileOfMarketingUpdates,
        true
    )
}

private fun NotificationsActivity.displayBackingsNotificationSettings(
    user: User,
    binding: ActivityNotificationsBinding,
    viewModel: NotificationsViewModel.NotificationsViewModel
) {
    this.notifyMobileOfBackings = user.notifyMobileOfBackings().isTrue()
    this.notifyOfBackings = user.notifyOfBackings().isTrue()
    this.notifyOfCreatorDigest = user.notifyOfCreatorDigest().isTrue()

    val frequencyIndex = when {
        notifyOfCreatorDigest -> User.EmailFrequency.DAILY_SUMMARY.ordinal
        else -> User.EmailFrequency.TWICE_A_DAY_SUMMARY.ordinal
    }

    toggleImageButtonIconColor(binding.backingsPhoneIcon, this.notifyMobileOfBackings, true)
    toggleImageButtonIconColor(binding.backingsMailIcon, this.notifyOfBackings)

    if (frequencyIndex != binding.emailFrequencySpinner.selectedItemPosition) {
        binding.emailFrequencySpinner.setSelection(frequencyIndex, false)
    }

    binding.emailFrequencySpinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (frequencyIndex != position) {
                    viewModel.inputs.notifyOfCreatorDigest(position == User.EmailFrequency.DAILY_SUMMARY.ordinal)
                }
            }
        }
}

private fun NotificationsActivity.displayCommentsNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfComments = user.notifyMobileOfComments().isTrue()
    this.notifyOfComments = user.notifyOfComments().isTrue()

    toggleImageButtonIconColor(binding.commentsPhoneIcon, this.notifyMobileOfComments, true)
    toggleImageButtonIconColor(binding.commentsMailIcon, this.notifyOfComments)
}

private fun NotificationsActivity.displayCommentRepliesNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfCommentReplies = user.notifyOfCommentReplies().isTrue()
    toggleImageButtonIconColor(
        binding.commentRepliesMailIcon,
        this.notifyMobileOfCommentReplies
    )
}

private fun NotificationsActivity.displayCreatorTipsNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfCreatorEdu = user.notifyMobileOfCreatorEdu().isTrue()
    this.notifyOfCreatorEdu = user.notifyOfCreatorEdu().isTrue()

    toggleImageButtonIconColor(binding.creatorEduPhoneIcon, this.notifyMobileOfCreatorEdu, true)
    toggleImageButtonIconColor(binding.creatorEduMailIcon, this.notifyOfCreatorEdu)
}

private fun NotificationsActivity.displayFollowerNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfFollower = user.notifyMobileOfFollower().isTrue()
    this.notifyOfFollower = user.notifyOfFollower().isTrue()

    toggleImageButtonIconColor(binding.newFollowersPhoneIcon, this.notifyMobileOfFollower, true)
    toggleImageButtonIconColor(binding.newFollowersMailIcon, this.notifyOfFollower)
}

private fun NotificationsActivity.displayFriendActivityNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfFriendActivity = user.notifyMobileOfFriendActivity().isTrue()
    this.notifyOfFriendActivity = user.notifyOfFriendActivity().isTrue()

    toggleImageButtonIconColor(
        binding.friendActivityPhoneIcon,
        this.notifyMobileOfFriendActivity,
        true
    )
    toggleImageButtonIconColor(binding.friendActivityMailIcon, this.notifyOfFriendActivity)
}

private fun NotificationsActivity.displayMessagesNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfMessages = user.notifyMobileOfMessages().isTrue()
    this.notifyOfMessages = user.notifyOfMessages().isTrue()

    toggleImageButtonIconColor(binding.messagesPhoneIcon, this.notifyMobileOfMessages, true)
    toggleImageButtonIconColor(binding.messagesMailIcon, this.notifyOfMessages)
}

private fun NotificationsActivity.displayPostLikesNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfPostLikes = user.notifyMobileOfPostLikes().isTrue()
    toggleImageButtonIconColor(binding.postLikesPhoneIcon, this.notifyMobileOfPostLikes, true)
}

private fun NotificationsActivity.displayUpdatesNotificationSettings(user: User, binding: ActivityNotificationsBinding) {
    this.notifyMobileOfUpdates = user.notifyMobileOfUpdates().isTrue()
    this.notifyOfUpdates = user.notifyOfUpdates().isTrue()

    toggleImageButtonIconColor(
        binding.projectUpdatesPhoneIcon,
        this.notifyMobileOfUpdates,
        true
    )
    toggleImageButtonIconColor(binding.projectUpdatesMailIcon, this.notifyOfUpdates)
}

internal fun NotificationsActivity.toggleImageButtonIconColor(
    imageButton: ImageButton,
    enabled: Boolean,
    typeMobile: Boolean = false
) {
    val color = if (enabled) R.color.kds_create_700 else R.color.kds_support_400
    imageButton.setColorFilter(ContextCompat.getColor(this, color))

    val background = if (enabled) R.drawable.circle_gray_filled else R.drawable.circle_gray_outline
    imageButton.setBackgroundResource(background)

    setContentDescription(imageButton, enabled, typeMobile)
}

private fun NotificationsActivity.setContentDescription(view: View, enabled: Boolean, typeMobile: Boolean) {
    val contentDescription = when {
        typeMobile && enabled -> getString(R.string.profile_settings_accessibility_unsubscribe_mobile_notifications)
        typeMobile && !enabled -> getString(R.string.profile_settings_accessibility_subscribe_mobile_notifications)
        !typeMobile && enabled -> getString(R.string.profile_settings_accessibility_unsubscribe_notifications)
        else -> getString(R.string.profile_settings_accessibility_subscribe_notifications)
    }
    view.contentDescription = contentDescription
}
