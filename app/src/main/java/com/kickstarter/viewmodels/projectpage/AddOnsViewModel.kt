package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.ProblemDetailsCopyMapper
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.pledgeAmountTotalPlusBonus
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
class AddOnsViewModel(
    private val environment: Environment,
    bundle: Bundle? = null,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentUser = requireNotNull(environment.currentUserV2())
    private var isUserLoggedIn = false

    private var currentUserReward: Reward = Reward.builder().build()
    private var pledgeData: PledgeData? = null
    private var projectData: ProjectData? = null
    private var project: Project = Project.builder().build()
    private var shippingRule: ShippingRule = ShippingRule.builder().build()
    private var pledgeflowcontext: PledgeFlowContext = PledgeFlowContext.NEW_PLEDGE
    private var bonusAmount: Double = 0.0
    private var pReason: PledgeReason = PledgeReason.PLEDGE
    private var backing: Backing? = null

    private var addOns = mutableListOf<Reward>()
    private var errorAction: (message: String?) -> Unit = {}
    private val currentSelection = mutableMapOf<Long, Int>()

    private var backedAddOns = emptyList<Reward>()

    // Pagination variables
    private var nextPage: String? = null
    var hasMorePages = false

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val mutableAddOnsUIState = MutableStateFlow(AddOnsUIState())
    val addOnsUIState: StateFlow<AddOnsUIState>
        get() = mutableAddOnsUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = AddOnsUIState()
            )

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    init {
        scope.launch {
            currentUser.observable().asFlow()
                .collectLatest {
                    isUserLoggedIn = it.isPresent()
                }
        }
    }

    /**
     * Used in Crowdfund checkout
     */
    fun provideBundle(bundle: Bundle?) {
        bundle?.let {
            val parsed = AddOnsBundleParser.parse(it)
            pledgeData = parsed.pledgeData
            sendEvent()
            pReason = parsed.pledgeReason
            projectData = parsed.projectData
            currentUserReward = parsed.currentUserReward
            bonusAmount = parsed.bonusAmount
            project = parsed.project
            shippingRule = parsed.shippingRule
            pledgeflowcontext = parsed.pledgeFlowContext
            backing = parsed.backing
            backedAddOns = parsed.backedAddOns

            scope.launch {
                getAddOns(shippingRule)
            }
        }
    }

    /**
     * Used in late pledges
     */
    fun provideProjectData(projectData: ProjectData) {
        this.projectData = projectData
        val isLatePledge = projectData.project().postCampaignPledgingEnabled() == true && projectData.project().isInPostCampaignPledgingPhase() == true
        val flowContext = if (isLatePledge) PledgeFlowContext.forPledgeReason(PledgeReason.LATE_PLEDGE) else PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        this.pledgeflowcontext = flowContext
        this.pledgeData = PledgeData.with(
            projectData = projectData,
            pledgeFlowContext = flowContext,
            reward = currentUserReward
        )
        this.projectData?.project()?.let {
            project = it
        }
    }

    /**
     * Used in late pledges
     */
    fun provideSelectedShippingRule(shippingRule: ShippingRule) {
        if (this.shippingRule != shippingRule) {
            this.shippingRule = shippingRule
            scope.launch {
                getAddOns(shippingRule)
            }
        }
    }

    fun loadMore() {
        scope.launch {
            getAddOns(shippingRule)
        }
    }

    private suspend fun getAddOns(selectedShippingRule: ShippingRule) {
        // - Do not execute call unless reward has addOns
        if (currentUserReward.hasAddons()) {
            emitCurrentState(isLoading = true)
            val envelopeResult = apolloClient.getRewardAllowedAddOns(
                rewardId = currentUserReward,
                locationId = selectedShippingRule.location() ?: Location.builder().build(),
                cursor = nextPage
            )

            if (envelopeResult.isSuccess) {
                val addOns = envelopeResult.getOrNull()?.addOnsList?.filter { addOn ->
                    RewardUtils.isAvailable(addOn)
                }

                // - pagination related stuff
                nextPage = envelopeResult.getOrNull()?.pageInfo?.endCursor
                hasMorePages = envelopeResult.getOrNull()?.pageInfo?.hasNextPage ?: false
                if (!addOns.isNullOrEmpty()) {
                    val updatedList = AddOnsFilteringHelper.getUpdatedList(
                        addOns,
                        backedAddOns,
                        selectedShippingRule.location() ?: Location.builder().build(),
                        currentSelection
                    )

                    this@AddOnsViewModel.addOns.addAll(updatedList)
                }
                emitCurrentState(isLoading = false)
            }

            if (envelopeResult.isFailure) {
                val throwable = envelopeResult.exceptionOrNull()
                val problem = throwable?.let { ProblemDetailsMapper.fromThrowable(it, "fetch project add-ons") }
                    ?: ProblemDetailsMapper.fromDomainMessage("Unable to load add-ons", "fetch project add-ons")
                errorAction.invoke(ProblemDetailsCopyMapper.displayMessage(problem))
            }
        } else {
            emitCurrentState(isLoading = false)
        }
    }

    // - User has selected a different reward clear previous states
    fun userRewardSelection(reward: Reward) {
        if (reward != currentUserReward) {
            currentUserReward = reward
            currentSelection.clear()
            bonusAmount = 0.0

            scope.launch {
                emitCurrentState()
            }
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableAddOnsUIState.emit(
            AddOnsUIState(
                addOns = addOns.toList(),
                isLoading = isLoading,
                totalCount = currentSelection.values.sum(),
                shippingRule = shippingRule,
                totalPledgeAmount = calculateTotalPledgeAmount(),
                totalBonusAmount = bonusAmount
            )
        )
    }

    /**
     * Callback to update the selected addOns quantity
     */
    fun updateSelection(rewardId: Long, quantity: Int) {
        scope.launch {
            currentSelection[rewardId] = quantity
            emitCurrentState(isLoading = false)
        }
    }

    fun getPledgeDataAndReason(): Pair<PledgeData, PledgeReason>? {
        val selectedAddOns = mutableListOf<Reward>()
        addOns.forEach {
            val amount = currentSelection[it.id()]
            if (amount != null && amount > 0) {
                selectedAddOns.add(it.toBuilder().quantity(amount).build())
            }
        }

        return projectData?.let {
            Pair(
                PledgeData.with(pledgeflowcontext, it, currentUserReward, selectedAddOns.toList(), shippingRule, bonusAmount = bonusAmount),
                pReason
            )
        }
    }

    fun isUserLoggedIn(): Boolean = isUserLoggedIn
    fun getProject() = this.project
    fun getSelectedReward() = this.currentUserReward
    fun sendEvent() = this.pledgeData?.let {
        environment.analytics()?.trackAddOnsScreenViewed(it)
    }

    fun bonusAmountUpdated(bAmount: Double) {
        scope.launch {
            bonusAmount = bAmount
            emitCurrentState(isLoading = false)
        }
    }

    private fun calculateTotalPledgeAmount(): Double {
        return getPledgeDataAndReason()?.first?.pledgeAmountTotalPlusBonus() ?: 0.0
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(
        private val environment: Environment,
        private val bundle: Bundle? = null,
        private val testDispatcher: CoroutineDispatcher? = null
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOnsViewModel(environment, bundle, testDispatcher) as T
        }
    }
}
