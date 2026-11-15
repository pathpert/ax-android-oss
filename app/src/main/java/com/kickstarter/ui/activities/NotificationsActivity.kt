package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNotificationsBinding
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.User
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.NotificationsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

/**
 * Activity displaying and coordinating user interactions for NotificationsActivity.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.notifications")
class NotificationsActivity : ComponentActivity() {

    private val unableToSaveString = R.string.profile_settings_error

    internal var notifyMobileOfBackings: Boolean = false
    internal var notifyMobileOfComments: Boolean = false
    internal var notifyMobileOfCommentReplies: Boolean = false
    internal var notifyMobileOfCreatorEdu: Boolean = false
    internal var notifyMobileOfFollower: Boolean = false
    internal var notifyMobileOfFriendActivity: Boolean = false
    internal var notifyMobileOfMessages: Boolean = false
    internal var notifyMobileOfPostLikes: Boolean = false
    internal var notifyMobileOfUpdates: Boolean = false
    internal var notifyMobileOfMarketingUpdates: Boolean = false
    internal var notifyOfBackings: Boolean = false
    internal var notifyOfComments: Boolean = false
    internal var notifyOfCreatorDigest: Boolean = false
    internal var notifyOfCreatorEdu: Boolean = false
    internal var notifyOfFollower: Boolean = false
    internal var notifyOfFriendActivity: Boolean = false
    internal var notifyOfMessages: Boolean = false
    internal var notifyOfUpdates: Boolean = false

    private lateinit var binding: ActivityNotificationsBinding

    private lateinit var viewModelFactory: NotificationsViewModel.Factory
    private val viewModel: NotificationsViewModel.NotificationsViewModel by viewModels { viewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        this.getEnvironment()?.let { env ->
            viewModelFactory = NotificationsViewModel.Factory(env)
            env
        }

        setContentView(binding.root)

        this.viewModel.outputs.creatorDigestFrequencyIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.emailFrequencyRow.isGone = it
            }.addToDisposable(disposables)

        this.viewModel.outputs.creatorNotificationsAreGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.creatorNotificationsSection.isGone = it
            }.addToDisposable(disposables)

        this.viewModel.outputs.user()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it, binding, viewModel) }
            .addToDisposable(disposables)

        this.viewModel.errors.unableToSavePreferenceError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ -> ViewUtils.showToast(this, getString(this.unableToSaveString)) }
            .addToDisposable(disposables)

        val emailFrequencyStrings = User.EmailFrequency.getStrings(this.resources)
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, emailFrequencyStrings)
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.emailFrequencySpinner.adapter = arrayAdapter

        setUpClickListeners(binding, viewModel)
    }

    internal fun startProjectNotificationsSettingsActivity() {
        startActivity(Intent(this, ProjectNotificationSettingsActivity::class.java))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
