package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.kickstarter.R
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.features.pledgedprojectsoverview.data.ApolloPledgedProjectsRepository
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PPO_PAGE_LIMIT
import com.kickstarter.features.pledgedprojectsoverview.data.PledgeTierType
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsPagingSource
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsRepository
import com.kickstarter.features.pledgedprojectsoverview.data.isTier1Type
import com.kickstarter.features.pledgedprojectsoverview.data.isTier2Type
import com.kickstarter.features.pledgedprojectsoverview.extensions.isTier2Type
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class PledgedProjectsOverviewUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val totalAlerts: Int? = null,
    val problem: ProblemDetails? = null
)

class PledgedProjectsOverviewViewModel(
    environment: Environment,
    private val ioDispatcher: CoroutineDispatcher,
    private val repository: PledgedProjectsRepository = ApolloPledgedProjectsRepository(
        requireNotNull(environment.apolloClientV2())
    )
) : ViewModel() {

    private val mutablePpoCards = MutableStateFlow<PagingData<PPOCard>>(PagingData.empty())
    private var mutableProjectFlow = MutableSharedFlow<Project>()
    private var snackbarMessage: (stringID: Int, type: String, duration: SnackbarDuration) -> Unit = { _, _, _ -> }
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val analyticEvents = requireNotNull(environment.analytics())
    private var isV2Enabled = environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_PLEDGED_PROJECTS_OVERVIEW_V2) ?: false
    private var isV1Enabled = environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_PLEDGED_PROJECTS_OVERVIEW) ?: false
    private val currentUser = requireNotNull(environment.currentUserV2())

    private var totalAlerts: Int? = null

    private val mutablePPOUIState = MutableStateFlow(PledgedProjectsOverviewUIState())
    val ppoCardsState: StateFlow<PagingData<PPOCard>> = mutablePpoCards.asStateFlow()

    private var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    private var pagingSource = PledgedProjectsPagingSource(
        apolloClient = apolloClient,
        analyticEvents = analyticEvents,
        limit = PPO_PAGE_LIMIT,
        tierTypes = getAllowedTierTypes()
    )

    val ppoUIState: StateFlow<PledgedProjectsOverviewUIState>
        get() = mutablePPOUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = PledgedProjectsOverviewUIState()
            )

    val projectFlow: SharedFlow<Project>
        get() = mutableProjectFlow
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed()
            )

    init {
        viewModelScope.launch(ioDispatcher) {
            emitCurrentState(isLoading = true)
            currentUser.observable().asFlow()
                .collectLatest {
                    totalAlerts = it.getValue()?.backingActionCount()
                }
        }
        getPledgedProjects()
    }

    fun getPledgedProjects() {
        viewModelScope.launch(ioDispatcher) {
            try {
                Pager(
                    PagingConfig(
                        pageSize = PPO_PAGE_LIMIT,
                        prefetchDistance = 3,
                        enablePlaceholders = true
                    )
                ) {
                    pagingSource
                }
                    .flow
                    .cachedIn(viewModelScope)
                    .map { pagingData ->
                        pagingData.filter {
                            if (!isV2Enabled) !(it.viewType ?: PPOCardViewType.UNKNOWN).isTier2Type() else true
                        }
                    }
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }.catch { throwable ->
                        emitCurrentState(
                            isErrored = true,
                            problem = ProblemDetailsMapper.fromThrowable(
                                throwable = throwable,
                                operation = "load pledged projects overview"
                            )
                        )
                    }
                    .collectLatest { pagingData ->
                        mutablePpoCards.value = pagingData
                        emitCurrentState()
                    }
            } catch (e: Exception) {
                emitCurrentState(
                    isErrored = true,
                    problem = ProblemDetailsMapper.fromThrowable(
                        throwable = e,
                        operation = "load pledged projects overview"
                    )
                )
            }
        }
    }

    fun confirmAddress(addressID: String, backingID: String) {
        viewModelScope.launch(ioDispatcher) {
            emitCurrentState(isLoading = true)
            when (val result = repository.confirmAddress(addressID, backingID)) {
                is DomainResult.Success -> {
                    showHeadsUpSnackbar(R.string.Address_confirmed_need_to_change_your_address_before_it_locks)
                    getPledgedProjects()
                    emitCurrentState()
                }
                is DomainResult.Failure -> {
                    showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                    emitCurrentState(problem = result.problem)
                }
            }
        }
    }

    fun onRewardRecievedChanged(backingID: String, checked: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            when (val result = repository.updateRewardReceived(backingID, checked)) {
                is DomainResult.Success -> emitCurrentState()
                is DomainResult.Failure -> {
                    showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                    emitCurrentState(problem = result.problem)
                }
            }
        }
    }

    fun onMessageCreatorClicked(projectName: String, projectId: String, creatorID: String, ppoCards: List<PPOCard?>, totalAlerts: Int) {
        viewModelScope.launch(ioDispatcher) {
            analyticEvents.trackPPOMessageCreatorCTAClicked(
                projectID = projectId,
                ppoCards = ppoCards,
                totalCount = totalAlerts,
                creatorID = creatorID
            )
            emitCurrentState(isLoading = true)
            when (val result = repository.fetchProjectBySlug(projectName)) {
                is DomainResult.Success -> {
                    mutableProjectFlow.emit(result.value)
                    emitCurrentState()
                }
                is DomainResult.Failure -> {
                    showErrorSnackbar(R.string.Something_went_wrong_please_try_again)
                    emitCurrentState(problem = result.problem)
                }
            }
        }
    }

    fun provideSnackbarMessage(snackBarMessage: (Int, String, SnackbarDuration) -> Unit) {
        this.snackbarMessage = snackBarMessage
    }

    suspend fun showLoadingState(isLoading: Boolean) {
        emitCurrentState(isLoading = isLoading)
    }

    private suspend fun emitCurrentState(
        isLoading: Boolean = false,
        isErrored: Boolean = false,
        problem: ProblemDetails? = null
    ) {
        mutablePPOUIState.emit(
            PledgedProjectsOverviewUIState(
                isLoading = isLoading,
                isErrored = isErrored,
                totalAlerts = totalAlerts,
                problem = problem
            )
        )
    }

    private fun getAllowedTierTypes(): List<PledgeTierType> {
        val list = PledgeTierType.values().toMutableList()
        list.remove(PledgeTierType.REWARD_RECEIVED)

        if (isV1Enabled.isFalse()) list.removeAll { it.isTier1Type() }
        if (isV2Enabled.isFalse()) list.removeAll { it.isTier2Type() }

        return list
    }

    fun showHeadsUpSnackbar(messageId: Int, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessage.invoke(messageId, KSSnackbarTypes.KS_HEADS_UP.name, duration)
    }

    fun showErrorSnackbar(messageId: Int, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessage.invoke(messageId, KSSnackbarTypes.KS_ERROR.name, duration)
    }

    fun sendFinalizePledgeCTAEvent(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        this@PledgedProjectsOverviewViewModel.analyticEvents.trackPPOFinalizePledgeCTAClicked(
            projectID = projectID,
            ppoCards = ppoCards,
            totalCount = totalCount
        )
    }

    class Factory(
        private val environment: Environment,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PledgedProjectsOverviewViewModel::class.java)) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }
            val vm = PledgedProjectsOverviewViewModel(environment, ioDispatcher)
            return modelClass.cast(vm)
                ?: throw IllegalStateException("Failed to cast ${vm::class.java.name} to ${modelClass.name}")
        }
    }
}
