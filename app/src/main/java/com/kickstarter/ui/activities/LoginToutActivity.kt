package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.coalesceWithV2
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.models.chrome.ChromeTabsHelper
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.login.LoginToutScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.LoginToutViewModel
import com.kickstarter.viewmodels.OAuthException
import com.kickstarter.viewmodels.OAuthViewModel
import com.kickstarter.viewmodels.OAuthViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber


@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.login")
class LoginToutActivity : ComponentActivity() {

    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: LoginToutViewModel.Factory
    private val viewModel: LoginToutViewModel.LoginToutViewmodel by viewModels {
        viewModelFactory
    }

    private lateinit var environment: Environment

    private val disposables = CompositeDisposable()

    private lateinit var oAuthViewModelFactory: OAuthViewModelFactory
    private val oAuthViewModel: OAuthViewModel by viewModels {
        oAuthViewModelFactory
    }

    private val oAuthLogcat = "OAuth: "

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val url = it.getStringExtra(IntentKey.OAUTH_REDIRECT_URL) ?: ""
                // - Redirection takes place from WebView, as default browser is not Chrome
                afterRedirection(url, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = LoginToutViewModel.Factory(env)
            oAuthViewModelFactory = OAuthViewModelFactory(environment = env)
            this.ksString = requireNotNull(env.ksString())

            oAuthViewModel.provideErrorAction { cause ->
                FirebaseCrashlytics.getInstance().recordException(OAuthException(cause))
            }
        }

        setContent {
            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            KickstarterApp(useDarkTheme = darModeEnabled) {
                LoginToutScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onFacebookButtonClicked = { facebookLoginClick() },
                    onTermsOfUseClicked = { viewModel.inputs.disclaimerItemClicked(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = {
                        viewModel.inputs.disclaimerItemClicked(
                            DisclaimerItems.PRIVACY
                        )
                    },
                    onCookiePolicyClicked = { viewModel.inputs.disclaimerItemClicked(DisclaimerItems.COOKIES) },
                    onHelpClicked = {
                        viewModel.inputs.disclaimerItemClicked(DisclaimerItems.HELP)
                    },
                    onSignUpOrLogInClicked = {
                        oAuthViewModel.sendCTAEvent()
                        oAuthViewModel.produceState(intent = intent)
                    }
                )
            }
        }

        logInAndSignUpAndLoginWithFacebookVM()

        setUpOAuthViewModel()

        intent.data?.let { data ->
            afterRedirection(data.toString(), intent)
        }
    }

    /***
     * Handles the the viewModel RXJava subscriptions for the user cases:
     * - LogIn non OAuth
     * - SignUp non OAuth
     * - LogIn with Facebook
     */
    private fun logInAndSignUpAndLoginWithFacebookVM() {
        val loginReason = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(IntentKey.LOGIN_REASON, LoginReason::class.java)
        } else {
            intent.getSerializableExtra(IntentKey.LOGIN_REASON) as LoginReason?
        }

        loginReason?.let {
            viewModel.provideLoginReason(it)
        }

        viewModel.outputs.finishWithSuccessfulResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishWithSuccessfulResult() }
            .addToDisposable(disposables)

        viewModel.outputs.startFacebookConfirmationActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startFacebookConfirmationActivity(it.first, it.second) }
            .addToDisposable(disposables)

        viewModel.outputs.showFacebookAuthorizationErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { LoginToutDialogHelper.showFacebookAuthorizationError(this) }
            .addToDisposable(disposables)

        showErrorMessageToasts()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, it ?: "") }
            .addToDisposable(disposables)

        viewModel.outputs.showUnauthorizedErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { LoginToutDialogHelper.showUnauthorizedError(this, it) }
            .addToDisposable(disposables)

        viewModel.outputs.showFacebookErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { LoginToutDialogHelper.showFacebookError(this, viewModel) }
            .addToDisposable(disposables)

        viewModel.outputs.showDisclaimerActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startDisclaimerChromeTab(it, environment)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startResetPasswordActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startResetActivity()
            }
            .addToDisposable(disposables)

        viewModel.outputs.finishOauthWithSuccessfulResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishWithSuccessfulResult() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        Timber.d("$oAuthLogcat onDestroy")
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("$oAuthLogcat onNewIntent Intent: $intent, data: ${intent?.data}")
        intent.data?.let { data ->
            val url = data.toString()
            // - Redirection takes place from ChromeTab, as the defaultBrowser is Chrome
            afterRedirection(url, intent)
        }
    }

    private fun afterRedirection(url: String, intent: Intent) {
        try {
            val uri = url.toUri()
            uri?.let {
                if (OAuthViewModel.isAfterRedirectionStep(it))
                    oAuthViewModel.produceState(intent = intent, uri)
            }
        } catch (error: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(OAuthException(error))
        }
    }

    private fun setUpOAuthViewModel() {
        lifecycleScope.launch {
            oAuthViewModel.uiState.collect { state ->
                // - Intent generated with onCreate
                if (state.isAuthorizationStep && state.authorizationUrl.isNotEmpty()) {
                    openChromeTabOrWebViewWithUrl(state.authorizationUrl)
                }

                if (state.user.isNotNull()) {
                    setResult(RESULT_OK)
                    this@LoginToutActivity.finish()
                }
            }
        }
    }

    private fun openChromeTabOrWebViewWithUrl(url: String) {
        LoginToutNavigationHelper.openChromeTabOrWebViewWithUrl(this, url, startForResult)
    }

    private fun facebookLoginClick() =
        viewModel.inputs.facebookLoginClick(
            this,
            resources.getStringArray(R.array.facebook_permissions_array).asList()
        )

    private fun showErrorMessageToasts(): Observable<String?> {
        return viewModel.outputs.showMissingFacebookEmailErrorToast()
            .map(coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            .mergeWith(
                viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
                    .map(coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            )
    }

    private fun finishWithSuccessfulResult() {
        setResult(RESULT_OK)
        LoginToutNavigationHelper.goToSurveyIfSurveyPresent(this, intent)
        finish()
    }

    private fun startResetActivity() {
        LoginToutNavigationHelper.startResetActivity(this)
    }

    private fun startFacebookConfirmationActivity(
        facebookUser: FacebookUser,
        accessTokenString: String
    ) {
        LoginToutNavigationHelper.startFacebookConfirmationActivity(
            this,
            facebookUser,
            accessTokenString
        )
    }

    @Deprecated("Needs to be replaced with new method, but requires request code usage to go away as well")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.provideOnActivityResult(create(requestCode, resultCode, intent))
    }
}
