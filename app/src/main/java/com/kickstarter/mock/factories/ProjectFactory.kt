package com.kickstarter.mock.factories

import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import com.kickstarter.type.CreditCardTypes
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Mock factory producing Kickstarter [Project] instances for testing and preview mocks.
 */
object ProjectFactory {

    @JvmStatic
    fun project(): Project {
        val creator = UserFactory.creator()
        val slug = "slug-1"
        val projectUrl = "https://www.kickstarter.com/projects/" + creator.id() + "/" + slug
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()
        return Project.builder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.DINERS.rawValue,
                    CreditCardTypes.DISCOVER.rawValue,
                    CreditCardTypes.JCB.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.UNIONPAY.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .backersCount(100)
            .blurb("Some blurb")
            .category(CategoryFactory.category())
            .creator(UserFactory.creator())
            .country("US")
            .countryCode("US")
            .createdAt(DateTime.now(DateTimeZone.UTC))
            .currency("USD")
            .currencySymbol("$")
            .currentCurrency("USD")
            .currencyTrailingCode(true)
            .fxRate(1.0f)
            .goal(100.0)
            .prelaunchActivated(false)
            .watchesCount(100)
            .id(IdFactory.id().toLong())
            .location(LocationFactory.unitedStates())
            .name("Some Name")
            .pledged(50.0)
            .percentFunded(50)
            .photo(PhotoFactory.photo())
            .rewards(listOf(RewardFactory.noReward(), RewardFactory.reward()))
            .staffPick(false)
            .state(Project.STATE_LIVE)
            .staticUsdRate(1.0f)
            .usdExchangeRate(1.0f)
            .sendMetaCapiEvents(false)
            .slug(slug)
            .projectFaqs(ProjectFaqFactory.getFaqs())
            .envCommitments(ProjectEnvironmentalCommitmentFactory.getEnvironmentalCommitments())
            .updatedAt(DateTime.now())
            .urls(Urls.builder().web(web).build())
            .video(VideoFactory.video())
            .sendThirdPartyEvents(true)
            .launchedAt(DateTime(DateTimeZone.UTC).minusDays(10))
            .deadline(DateTime(DateTimeZone.UTC).plusDays(10))
            .risks("Risks and challenges")
            .pledgeOverTimeCollectionPlanChargeExplanation("The first charge will occur when the project ends successfully, then every month until fully paid. When this option is selected no further edits can be made to your pledge. See our Terms of Use.")
            .pledgeOverTimeCollectionPlanChargedAsNPayments("charged as 3 payments")
            .pledgeOverTimeCollectionPlanShortPitch("You will be charged for your pledge over four payments, at no extra cost.")
            .build()
    }

    // Delegated Backed Projects
    @JvmStatic fun backedProjectWithError(): Project = BackedProjectFactory.backedProjectWithError()
    @JvmStatic fun backedProject(): Project = BackedProjectFactory.backedProject()
    @JvmStatic fun backedProjectWithPMCheckoutOrder(): Project = BackedProjectFactory.backedProjectWithPMCheckoutOrder()
    @JvmStatic fun backedProjectWithPlotSelected(): Project = BackedProjectFactory.backedProjectWithPlotSelected()
    @JvmStatic fun backedProjectWithRewardAndAddOnsLimitReached(): Project = BackedProjectFactory.backedProjectWithRewardAndAddOnsLimitReached()
    @JvmStatic fun backedProjectWithAddOns(): Project = BackedProjectFactory.backedProjectWithAddOns()
    @JvmStatic fun backedProjectRewardAvailableAddOnsNotBackedAddOns(): Project = BackedProjectFactory.backedProjectRewardAvailableAddOnsNotBackedAddOns()
    @JvmStatic fun backedSuccessfulProject(): Project = BackedProjectFactory.backedSuccessfulProject()
    @JvmStatic fun backedProjectWithRewardLimited(): Project = BackedProjectFactory.backedProjectWithRewardLimited()
    @JvmStatic fun backedProjectWithRewardLimitReached(): Project = BackedProjectFactory.backedProjectWithRewardLimitReached()
    @JvmStatic fun backedProjectWithNoReward(): Project = BackedProjectFactory.backedProjectWithNoReward()

    // Delegated International Projects
    @JvmStatic fun britishProject(): Project = InternationalProjectFactory.britishProject()
    @JvmStatic fun caProject(): Project = InternationalProjectFactory.caProject()
    @JvmStatic fun mxCurrencyCAProject(): Project = InternationalProjectFactory.mxCurrencyCAProject()
    @JvmStatic fun mxProject(): Project = InternationalProjectFactory.mxProject()
    @JvmStatic fun ukProject(): Project = InternationalProjectFactory.ukProject()

    // Project State and Milestone Variants
    @JvmStatic
    fun projectWithActivePMForNewBackers(): Project {
        return project()
            .toBuilder()
            .pledgeManager(PledgeManagerFactory.pledgeManagerAcceptsNetNewBackers())
            .lastWave(CheckoutWaveFactory.checkoutWaveActive())
            .redemptionPageUrl("backing/redeem")
            .build()
    }

    @JvmStatic
    fun projectWithAddOns(): Project {
        val rwWithAddOn = RewardFactory.reward().toBuilder().hasAddons(true).build()
        val rw = RewardFactory.reward().toBuilder().hasAddons(false).build()
        return project().toBuilder().rewards(listOf(rw, rwWithAddOn)).build()
    }

    @JvmStatic
    fun halfWayProject(): Project {
        return project()
            .toBuilder()
            .name("halfwayProject")
            .goal(100.0)
            .pledged(50.0)
            .percentFunded(50)
            .build()
    }

    @JvmStatic
    fun allTheWayProject(): Project {
        return project()
            .toBuilder()
            .name("allTheWayProject")
            .goal(100.0)
            .pledged(100.0)
            .percentFunded(50)
            .build()
    }

    @JvmStatic
    fun doubledGoalProject(): Project {
        return project()
            .toBuilder()
            .name("doubledGoalProject")
            .goal(100.0)
            .pledged(200.0)
            .percentFunded(200)
            .build()
    }

    @JvmStatic
    fun failedProject(): Project {
        return project()
            .toBuilder()
            .name("failedProject")
            .state(Project.STATE_FAILED)
            .build()
    }

    @JvmStatic
    fun almostCompletedProject(): Project {
        return project()
            .toBuilder()
            .name("almostCompleteProject")
            .deadline(DateTime().plusDays(1))
            .build()
    }

    @JvmStatic
    fun successfulProject(): Project {
        return project()
            .toBuilder()
            .name("successfulProject")
            .deadline(DateTime().minus(2))
            .state(Project.STATE_SUCCESSFUL)
            .build()
    }

    @JvmStatic
    fun featured(): Project {
        return project()
            .toBuilder()
            .name("featuredProject")
            .featuredAt(DateTime())
            .build()
    }

    @JvmStatic
    fun saved(): Project {
        return project()
            .toBuilder()
            .name("savedProject")
            .isStarred(true)
            .build()
    }

    @JvmStatic
    fun staffPick(): Project {
        return project()
            .toBuilder()
            .name("staffPickProject")
            .staffPick(true)
            .build()
    }

    @JvmStatic
    fun prelaunchProject(projectUrl: String): Project {
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()

        return project()
            .toBuilder()
            .launchedAt(DateTime(0))
            .displayPrelaunch(true)
            .urls(Urls.builder().web(web).build())
            .build()
    }

    @JvmStatic
    fun initialProject(): Project {
        return project()
            .toBuilder()
            .rewards(null)
            .build()
    }
}
