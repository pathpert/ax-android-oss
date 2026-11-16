package com.kickstarter.viewmodels

import com.facebook.CallbackManager
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.services.apiresponses.ErrorEnvelope
import io.reactivex.disposables.CompositeDisposable

/**
 * Facebook callback registration and Rx pipeline setup for [LoginToutViewModel.LoginToutViewmodel].
 */
internal fun LoginToutViewModel.LoginToutViewmodel.registerFacebookCallback() {
    val callback = CallbackManager.Factory.create()
    callbackManager = callback

    LoginManager.getInstance().registerCallback(
        callback,
        object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                facebookAccessToken.onNext(result.accessToken.token)
            }

            override fun onCancel() {
                // continue
            }

            override fun onError(error: FacebookException) {
                val problem = ProblemDetailsMapper.fromThrowable(error, "authenticate with facebook")
                if (error is FacebookAuthorizationException) {
                    facebookAuthorizationError.onNext(error)
                }
            }
        }
    )
}

internal fun LoginToutViewModel.LoginToutViewmodel.initLoginToutPipelines(disposables: CompositeDisposable) {
    registerFacebookCallback()

    val facebookAccessTokenEnvelope = facebookAccessToken
        .switchMap {
            loginWithFacebookAccessToken(it)
        }
        .share()

    facebookAuthorizationError
        .subscribe { LoginManager.getInstance().logOut() }
        .addToDisposable(disposables)

    facebookAccessTokenEnvelope
        .compose(Transformers.valuesV2())
        .filter { it.isNotNull() }
        .switchMap {
            this.loginUserCase.loginAndUpdateUserPrivacy(it.user(), it.accessToken())
        }
        .subscribe {
            refreshUserUseCase.refresh(it)
            finishWithSuccessfulResult.onNext(Unit)
        }
        .addToDisposable(disposables)

    facebookAccessTokenEnvelope
        .compose(Transformers.errorsV2())
        .map { throwable ->
            val problem = ProblemDetailsMapper.fromThrowable(throwable, "login with facebook")
            ErrorEnvelope.fromThrowable(ProblemException(problem, throwable))
        }
        .filter { it.isNotNull() }
        .subscribe { loginError.onNext(it) }
        .addToDisposable(disposables)

    startFacebookConfirmationActivity = loginError
        .filter(ErrorEnvelope::isConfirmFacebookSignupError)
        .filter { it.facebookUser() != null }
        .map { it.facebookUser() }
        .compose(Transformers.combineLatestPair(facebookAccessToken))

    facebookAuthorizationError
        .filter {
            environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE) == true
        }
        .subscribe {
            showFacebookErrorDialog.onNext(Unit)
        }
        .addToDisposable(disposables)

    startLoginActivity = loginClick
    showDisclaimerActivity = disclaimerItemClicked

    facebookLoginClick
        .compose(Transformers.ignoreValuesV2())
        .subscribe {
            analyticEvents?.trackLoginOrSignUpCtaClicked(
                ContextTypeName.FACEBOOK.contextName,
                ContextPageName.LOGIN_SIGN_UP.contextName
            )
        }
        .addToDisposable(disposables)

    onResetPasswordFacebookErrorDialogClicked
        .subscribe { startResetPasswordActivity.onNext(Unit) }
        .addToDisposable(disposables)

    onLoginFacebookErrorDialogClicked
        .subscribe {
            loginClick.onNext(Unit)
        }
        .addToDisposable(disposables)

    currentUser.observable()
        .filter { it.isPresent() }
        .subscribe {
            if (currentUser.accessToken != null) {
                finishOauthWithSuccessfulResult.onNext(Unit)
            }
        }
        .addToDisposable(disposables)
}
