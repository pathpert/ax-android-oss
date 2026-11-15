package com.kickstarter.ui.activities

import android.content.Context
import android.content.Intent
import android.text.Html
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.databinding.MessagesLayoutBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.startActivityWithTransition
import java.math.RoundingMode

/**
 * Navigation and view helper methods for [MessagesActivity].
 */
internal object MessagesActivityNavigation {

    fun startBackingActivity(activity: MessagesActivity, projectAndBacker: BackingWrapper) {
        val intent = Intent(activity, BackingActivity::class.java)
            .putExtra(IntentKey.BACKING, projectAndBacker.backing)
            .putExtra(IntentKey.PROJECT, projectAndBacker.project)
            .putExtra(IntentKey.BACKER, projectAndBacker.user)
            .putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
        activity.startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    fun startProjectPageActivity(activity: MessagesActivity, project: Project) {
        val intent = Intent().getProjectIntent(activity)
            .putExtra(IntentKey.PROJECT, project)
        activity.startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    fun setBackingInfoView(
        context: Context,
        binding: MessagesLayoutBinding,
        ksCurrency: KSCurrency,
        ksString: KSString,
        backingAndProject: Pair<Backing, Project>
    ) {
        val pledgeAmount = ksCurrency.format(backingAndProject.first.amount(), backingAndProject.second, RoundingMode.HALF_UP)
        backingAndProject.first.pledgedAt()?.let {
            val pledgeDate = DateTimeUtils.relative(context, ksString, it)
            binding.messagesBackingInfoView.backingAmountTextView.text = Html.fromHtml(
                ksString.format(
                    context.getString(R.string.pledge_amount_pledged_on_pledge_date), "pledge_amount", pledgeAmount, "pledge_date", pledgeDate
                )
            )
        }
    }
}
