package com.kickstarter.features.backing.data

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.type.CreditCardTypes
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.PlotData
import com.kickstarter.ui.data.ProjectData
import com.stripe.android.model.CardBrand

/**
 * Pure boundary mapping functions for Backing presentation logic.
 *
 * Negative Constraints:
 * - Do NOT perform network I/O or mutate state in this mapper.
 * - Do NOT pass empty or invalid projects into [pledgeStatusData].
 */
object BackingStatusMapper {

    fun shouldHideShipping(backing: Backing): Boolean =
        backing.locationId().isNull() || backing.reward()?.let { rw ->
            RewardUtils.isLocalPickup(rw)
        } ?: true

    fun joinProjectDataAndReward(projectData: ProjectData): Pair<ProjectData, Reward> {
        val reward = projectData.backing()?.reward()
            ?: projectData.project().backing()?.reward()
            ?: projectData.project().backing()?.backedReward(projectData.project())
            ?: RewardFactory.noReward().toBuilder()
                .minimum(projectData.backing()?.amount() ?: 1.0)
                .build()

        return Pair(projectData, reward)
    }

    fun cardIssuer(paymentSource: PaymentSource): Either<String, Int> {
        return when (CreditCardPaymentType.safeValueOf(paymentSource.paymentType())) {
            CreditCardPaymentType.ANDROID_PAY -> Either.Right(R.string.googlepay_button_content_description)
            CreditCardPaymentType.APPLE_PAY -> Either.Right(R.string.apple_pay_content_description)
            CreditCardPaymentType.CREDIT_CARD -> Either.Left(
                StoredCard.issuer(
                    CreditCardTypes.safeValueOf(
                        paymentSource.type() ?: ""
                    )
                )
            )
            else -> Either.Left(CardBrand.Unknown.code)
        }
    }

    fun cardLogo(paymentSource: PaymentSource): Int {
        return when (CreditCardPaymentType.safeValueOf(paymentSource.paymentType())) {
            CreditCardPaymentType.ANDROID_PAY -> R.drawable.google_pay_mark
            CreditCardPaymentType.APPLE_PAY -> R.drawable.apple_pay_mark
            CreditCardPaymentType.CREDIT_CARD -> paymentSource.getCardTypeDrawable()
            CreditCardPaymentType.BANK_ACCOUNT -> R.drawable.credit_card
            else -> R.drawable.generic_bank_md
        }
    }

    fun pledgeStatusData(
        project: Project,
        backing: Backing,
        user: User,
        ksCurrency: KSCurrency,
        featureFlagClient: FeatureFlagClientType?,
        webEndpoint: String
    ): PledgeStatusData {
        val statusStringRes = if (!project.userIsCreator(user)) {
            when (project.state()) {
                Project.STATE_CANCELED -> R.string.The_creator_canceled_this_project_so_your_payment_method_was_never_charged
                Project.STATE_FAILED -> R.string.This_project_didnt_reach_its_funding_goal_so_your_payment_method_was_never_charged
                else -> when (backing.status()) {
                    Backing.STATUS_CANCELED -> R.string.You_canceled_your_pledge_for_this_project
                    Backing.STATUS_COLLECTED -> R.string.We_collected_your_pledge_for_this_project
                    Backing.STATUS_DROPPED -> R.string.Your_pledge_was_dropped_because_of_payment_errors
                    Backing.STATUS_AUTHENTICATION_REQUIRED,
                    Backing.STATUS_ERRORED -> {
                        if (backing.status() == Backing.STATUS_AUTHENTICATION_REQUIRED && backing.paymentIncrements.isNullOrEmpty()) {
                            R.string.We_cant_process_your_pledge_visit_the_project_alerts_dashboard_to_authenticate
                        } else if (!backing.paymentIncrements().isNullOrEmpty() && !project.isLive) {
                            R.string.We_cant_process_your_Pledge_Over_Time_payment
                        } else {
                            R.string.We_cant_process_your_pledge_Please_update_your_payment_method
                        }
                    }
                    Backing.STATUS_PLEDGED -> {
                        val isPlotFlag = featureFlagClient?.getBoolean(FlagKey.ANDROID_PLEDGE_OVER_TIME) == true
                        val hasIncrements = !backing.paymentIncrements().isNullOrEmpty()
                        if (isPlotFlag && hasIncrements && project.isLive) {
                            R.string.You_have_selected_pledge_over_time
                        } else if (isPlotFlag && hasIncrements && !project.isLive) {
                            R.string.We_collected_your_pledge_for_this_project
                        } else {
                            R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline
                        }
                    }
                    Backing.STATUS_PREAUTH -> R.string.We_re_processing_your_pledge_pull_to_refresh
                    else -> null
                }
            }
        } else {
            when (project.state()) {
                Project.STATE_CANCELED -> R.string.You_canceled_this_project_so_the_backers_payment_method_was_never_charged
                Project.STATE_FAILED -> R.string.Your_project_didnt_reach_its_funding_goal_so_the_backers_payment_method_was_never_charged
                else -> when (backing.status()) {
                    Backing.STATUS_CANCELED -> R.string.The_backer_canceled_their_pledge_for_this_project
                    Backing.STATUS_COLLECTED -> R.string.We_collected_the_backers_pledge_for_this_project
                    Backing.STATUS_DROPPED -> R.string.This_pledge_was_dropped_because_of_payment_errors
                    Backing.STATUS_ERRORED -> R.string.We_cant_process_this_pledge_because_of_a_problem_with_the_backers_payment_method
                    Backing.STATUS_PLEDGED -> R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline
                    Backing.STATUS_PREAUTH -> R.string.We_re_processing_this_pledge_pull_to_refresh
                    else -> null
                }
            }
        }

        val isPlot = !backing.paymentIncrements().isNullOrEmpty()
        val projectDeadline = project.deadline()?.let { DateTimeUtils.longDate(it) }
        val pledgeTotal = backing.amount()
        val pledgeTotalString = ksCurrency.format(pledgeTotal, project)
        val plotData = if (isPlot) {
            val plotAmountString = backing.paymentIncrements()?.first()?.paymentIncrementAmount?.formattedAmount
            val plotFirstScheduleCollection = backing.paymentIncrements()
                ?.first()?.scheduledCollection?.let { DateTimeUtils.longDate(it) }
            val fixPledgeUrl = "$webEndpoint/projects/${project.slug()}/backing/details"

            PlotData(
                plotAmount = plotAmountString,
                plotFirstScheduleCollection = plotFirstScheduleCollection,
                fixPledgeUrl = fixPledgeUrl
            )
        } else {
            null
        }

        return PledgeStatusData(
            statusStringRes,
            pledgeTotalString,
            projectDeadline,
            plotData
        )
    }
}
