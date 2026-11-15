package com.kickstarter.ui.activities

import com.kickstarter.databinding.ActivityNotificationsBinding
import com.kickstarter.libs.utils.AnimationUtils
import com.kickstarter.viewmodels.NotificationsViewModel

internal fun NotificationsActivity.setUpClickListeners(
    binding: ActivityNotificationsBinding,
    viewModel: NotificationsViewModel.NotificationsViewModel
) {
    binding.manageProjectNotifications.setOnClickListener {
        startProjectNotificationsSettingsActivity()
    }

    binding.backingsMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfBackings(!notifyOfBackings)
    }

    binding.backingsPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfBackings(!notifyMobileOfBackings)
    }

    binding.backingsRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.backingsPhoneIcon,
            binding.backingsMailIcon
        )
    }

    binding.commentsMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfComments(!notifyOfComments)
    }

    binding.commentsPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfComments(!notifyMobileOfComments)
    }

    binding.commentRepliesMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfCommentReplies(!notifyMobileOfCommentReplies)
    }

    binding.commentRepliesRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(null, binding.commentRepliesMailIcon)
    }

    binding.commentsRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.commentsPhoneIcon,
            binding.commentsMailIcon
        )
    }

    binding.creatorEduMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfCreatorEdu(!notifyOfCreatorEdu)
    }

    binding.creatorEduPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfCreatorEdu(!notifyMobileOfCreatorEdu)
    }

    binding.creatorEduRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.creatorEduPhoneIcon,
            binding.creatorEduMailIcon
        )
    }

    binding.friendActivityMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfFriendActivity(!notifyOfFriendActivity)
    }

    binding.friendActivityPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfFriendActivity(!notifyMobileOfFriendActivity)
    }

    binding.friendsBackProjectRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.friendActivityPhoneIcon,
            binding.friendActivityMailIcon
        )
    }

    binding.messagesMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfMessages(!notifyOfMessages)
    }

    binding.messagesPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfMessages(!notifyMobileOfMessages)
    }

    binding.messagesNotificationRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.messagesPhoneIcon,
            binding.messagesMailIcon
        )
    }

    binding.newFollowersMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfFollower(!notifyOfFollower)
    }

    binding.newFollowersPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfFollower(!notifyMobileOfFollower)
    }

    binding.newFollowersRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.newFollowersPhoneIcon,
            binding.newFollowersMailIcon
        )
    }

    binding.postLikesPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfPostLikes(!notifyMobileOfPostLikes)
    }

    binding.projectUpdatesMailIcon.setOnClickListener {
        viewModel.inputs.notifyOfUpdates(!notifyOfUpdates)
    }

    binding.projectUpdatesPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfUpdates(!notifyMobileOfUpdates)
    }

    binding.marketingUpdatesPhoneIcon.setOnClickListener {
        viewModel.inputs.notifyMobileOfMarketingUpdate(!notifyMobileOfMarketingUpdates)
    }

    binding.projectUpdatesRow.setOnClickListener {
        AnimationUtils.notificationBounceAnimation(
            binding.projectUpdatesPhoneIcon,
            binding.projectUpdatesMailIcon
        )
    }
}
