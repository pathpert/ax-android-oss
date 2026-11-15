@file:JvmMultifileClass
@file:JvmName("ProjectExt")

package com.kickstarter.libs.utils.extensions

import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.Web

/**
 * Extension function that will return a reduced copy of the target project. The fields available
 * on the reduced copy are those required on screens like BackingAddons, PledgeFragment, ThanksActivity.
 *
 * The end goal is to reduce to the bare minimum the amount of memory required to be serialized on Intents
 * when presenting screens in order to avoid `android.os.TransactionTooLargeException`.
 */
fun Project.reduce(): Project {
    val web = Web.builder()
        .project(this.webProjectUrl())
        .build()

    return Project.Builder()
        .id(this.id())
        .slug(this.slug())
        .name(this.name())
        .location(this.location())
        .deadline(this.deadline())
        .staticUsdRate(this.staticUsdRate())
        .fxRate(this.fxRate())
        .country(this.country())
        .countryCode(this.countryCode())
        .currentCurrency(this.currentCurrency())
        .currency(this.currency())
        .currencySymbol(this.currencySymbol())
        .currencyTrailingCode(this.currencyTrailingCode())
        .sendThirdPartyEvents(this.sendThirdPartyEvents())
        .isBacking(this.isBacking())
        .isPledgeOverTimeAllowed(this.isPledgeOverTimeAllowed())
        .pledgeOverTimeMinimumExplanation(this.pledgeOverTimeMinimumExplanation())
        .pledgeOverTimeCollectionPlanShortPitch(this.pledgeOverTimeCollectionPlanShortPitch())
        .pledgeOverTimeCollectionPlanChargedAsNPayments(this.pledgeOverTimeCollectionPlanChargedAsNPayments())
        .pledgeOverTimeCollectionPlanChargeExplanation(this.pledgeOverTimeCollectionPlanChargeExplanation())
        .backing(this.backing())
        .availableCardTypes(this.availableCardTypes())
        .category(this.category())
        .urls(Urls.builder().web(web).build())
        .build()
}

/**
 * Extension function that will return a reduced copy of the target project payload.
 *
 * The end goal is to reduce to the bare minimum the amount of memory required to be serialized on Intents
 * when presenting screens in order to avoid `android.os.TransactionTooLargeException`.
 */
fun Project.reduceProjectPayload(): Project {
    val web = Web.builder()
        .project(this.webProjectUrl())
        .build()

    return Project.Builder()
        .id(this.id())
        .canComment(this.canComment())
        .slug(this.slug())
        .name(this.name())
        .creator(this.creator())
        .blurb(this.blurb())
        .location(this.location())
        .category(this.category())
        .watchesCount(this.watchesCount())
        .photo(this.photo())
        .country(this.country())
        .countryCode(this.countryCode())
        .currentCurrency(this.currentCurrency())
        .sendThirdPartyEvents(this.sendThirdPartyEvents())
        .isStarred(this.isStarred())
        .isBacking(this.isBacking())
        .currency(this.currency())
        .currencySymbol(this.currencySymbol())
        .currencyTrailingCode(this.currencyTrailingCode())
        .urls(Urls.builder().web(web).build())
        .isInPostCampaignPledgingPhase(this.isInPostCampaignPledgingPhase())
        .postCampaignPledgingEnabled(this.postCampaignPledgingEnabled())
        .isPledgeOverTimeAllowed(this.isPledgeOverTimeAllowed())
        .pledgeOverTimeMinimumExplanation(this.pledgeOverTimeMinimumExplanation())
        .pledgeOverTimeCollectionPlanShortPitch(this.pledgeOverTimeCollectionPlanShortPitch())
        .pledgeOverTimeCollectionPlanChargedAsNPayments(this.pledgeOverTimeCollectionPlanChargedAsNPayments())
        .pledgeOverTimeCollectionPlanChargeExplanation(this.pledgeOverTimeCollectionPlanChargeExplanation())
        .state(this.state())
        .build()
}
