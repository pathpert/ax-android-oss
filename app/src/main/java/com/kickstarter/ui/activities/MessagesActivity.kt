package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.kickstarter.R
import com.kickstarter.databinding.MessagesLayoutBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.MessagesAdapter
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.extensions.startBackingDetailsWebViewActivity
import com.kickstarter.viewmodels.MessagesViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.RoundingMode


@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.messages")
class MessagesActivity : AppCompatActivity() {
    private lateinit var ksCurrency: KSCurrency
    private lateinit var ksString: KSString
    private lateinit var adapter: MessagesAdapter

    private lateinit var binding: MessagesLayoutBinding

    private lateinit var viewModelFactory: MessagesViewModel.Factory
    private val viewModel: MessagesViewModel.MessagesViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the window to allow drawing under the system bars (status, nav bars),
        // making them transparent, and enabling edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = MessagesLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MessagesActivityInsetsHelper.setupWindowInsets(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = MessagesViewModel.Factory(env, intent)
            env
        }

        ksCurrency = requireNotNull(environment?.ksCurrency())
        ksString = requireNotNull(environment?.ksString())
        adapter = MessagesAdapter()

        binding.messagesRecyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = layoutManager

        binding.messagesBackingInfoView.messagesViewPledgeButton.text = getString(R.string.project_view_button)

        MessagesActivityInsetsHelper.setAppBarOffsetChangedListener(binding, viewModel)

        binding.messageReplyLayout.messageEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            viewModel.inputs.messageEditTextIsFocused(hasFocus)
        }

        viewModel.outputs.backButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.setGone(binding.messagesToolbar.messagesToolbarBackButton, it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.backingAndProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackingInfoView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.backingInfoViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesBackingInfoView.backingInfoView, it) }
            .addToDisposable(disposables)

        viewModel.outputs.closeButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesToolbar.messagesToolbarCloseButton, it) }
            .addToDisposable(disposables)

        viewModel.outputs.creatorNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesCreatorNameTextView.text = ksString.format(getString(R.string.project_creator_by_creator), "creator_name", it) }
            .addToDisposable(disposables)

        viewModel.outputs.loadingIndicatorViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesLoadingIndicator, it) }
            .addToDisposable(disposables)

        viewModel.outputs.messageEditTextHint()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setMessageEditTextHint(it) }
            .addToDisposable(disposables)

        viewModel.outputs.messageEditTextShouldRequestFocus()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { requestFocusAndOpenKeyboard() }
            .addToDisposable(disposables)

        viewModel.outputs.messageList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.messages(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesProjectNameTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameToolbarTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesToolbar.messagesProjectNameCollapsedTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.recyclerViewDefaultBottomPadding()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setDefaultRecyclerViewBottomPadding() }
            .addToDisposable(disposables)

        viewModel.outputs.recyclerViewInitialBottomPadding()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setInitialRecyclerViewBottomPadding(it) }
            .addToDisposable(disposables)

        viewModel.outputs.scrollRecyclerViewToBottom()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1) }
            .addToDisposable(disposables)

        viewModel.outputs.setMessageEditText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageReplyLayout.messageEditText.setText(it) }
            .addToDisposable(disposables)

        viewModel.outputs.sendMessageButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageReplyLayout.sendMessageButton.isEnabled = it }
            .addToDisposable(disposables)

        viewModel.outputs.showMessageErrorToast()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, it) }
            .addToDisposable(disposables)

        viewModel.outputs.startBackingActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startBackingActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectPageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectPageActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startBackingDetailsWebActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startBackingDetailsWebViewActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.toolbarIsExpanded()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesAppBarLayout.setExpanded(it) }
            .addToDisposable(disposables)

        viewModel.outputs.viewPledgeButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesBackingInfoView.messagesViewPledgeButton, it) }
            .addToDisposable(disposables)

        binding.messagesBackingInfoView.messagesViewPledgeButton.setOnClickListener {
            viewPledgeButtonClicked()
        }

        binding.messagesProjectContainerView.setOnClickListener {
            projectContainerViewClicked()
        }

        binding.messageReplyLayout.sendMessageButton.setOnClickListener {
            sendMessageButtonClicked()
        }

        binding.messagesToolbar.messagesToolbarBackButton.setOnClickListener {
            finishWithAnimation()
        }

        binding.messagesToolbar.messagesToolbarCloseButton.setOnClickListener {
            finishWithAnimation()
        }

        binding.messageReplyLayout.messageEditText.doOnTextChanged { message, _, _, _ ->
            message?.let { onMessageEditTextChanged(it) }
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    private fun sendMessageButtonClicked() =
        viewModel.inputs.sendMessageButtonClicked()

    private fun viewPledgeButtonClicked() =
        viewModel.inputs.viewPledgeButtonClicked()

    private fun projectContainerViewClicked() =
        viewModel.inputs.projectContainerViewClicked()

    private fun onMessageEditTextChanged(message: CharSequence) =
        viewModel.inputs.messageEditTextChanged(message.toString())

    override fun onDestroy() {
        super.onDestroy()
        binding.messagesRecyclerView.adapter = null
        disposables.clear()
    }

    private fun requestFocusAndOpenKeyboard() {
        binding.messageReplyLayout.messageEditText.requestFocus()
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }



    private fun setBackingInfoView(backingAndProject: Pair<Backing, Project>) =
        MessagesActivityNavigation.setBackingInfoView(this, binding, ksCurrency, ksString, backingAndProject)

    private fun setDefaultRecyclerViewBottomPadding() =
        binding
            .messagesRecyclerView
            .setPadding(0, 0, 0, resources.getDimension(R.dimen.message_reply_layout_height).toInt())

    private fun setInitialRecyclerViewBottomPadding(bottomPadding: Int) =
        binding
            .messagesRecyclerView
            .setPadding(0, 0, 0, (bottomPadding + resources.getDimension(R.dimen.message_reply_layout_height)).toInt())

    private fun setMessageEditTextHint(name: String) {
        binding.messageReplyLayout.messageEditText.hint = ksString.format(getString(R.string.Message_user_name), "user_name", name)
    }

    private fun startBackingActivity(projectAndBacker: BackingWrapper) =
        MessagesActivityNavigation.startBackingActivity(this, projectAndBacker)

    private fun startProjectPageActivity(project: Project) =
        MessagesActivityNavigation.startProjectPageActivity(this, project)
}
