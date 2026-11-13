package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.kickstarter.KSApplication
import com.kickstarter.features.splash.data.CustomNetworkClient
import com.kickstarter.features.splash.data.InitializationState
import com.kickstarter.features.splash.data.NavigationTarget
import com.kickstarter.features.splash.data.SplashOutputs
import com.kickstarter.features.splash.data.SplashUIState
import com.kickstarter.features.splash.viewmodel.SplashFeatureViewModel
import com.kickstarter.libs.Environment
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

typealias InitializationState = com.kickstarter.features.splash.data.InitializationState
typealias SplashUIState = com.kickstarter.features.splash.data.SplashUIState
typealias NavigationTarget = com.kickstarter.features.splash.data.NavigationTarget
typealias CustomNetworkClient = com.kickstarter.features.splash.data.CustomNetworkClient

/**
 * Legacy monolithic ViewModel facade delegating to modern [SplashFeatureViewModel].
 */
@Deprecated(
    message = "Legacy monolithic ViewModel replaced by SplashFeatureViewModel under features/splash/.",
    replaceWith = ReplaceWith(
        expression = "SplashFeatureViewModel",
        imports = ["com.kickstarter.features.splash.viewmodel.SplashFeatureViewModel"]
    )
)
interface SplashScreenViewModel {
    interface Outputs : SplashOutputs

    class DeepLinkViewModel(
        environment: Environment,
        application: KSApplication,
        intent: Intent?,
        externalCall: CustomNetworkClient
    ) : SplashFeatureViewModel(environment, application, intent, externalCall), Outputs

    class Factory(
        private val environment: Environment,
        private val intent: Intent? = null,
        private val customNetworkClient: CustomNetworkClient? = null
    ) : ViewModelProvider.Factory {
        private var externalCall = object : CustomNetworkClient {
            override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
                val httpClient: OkHttpClient = OkHttpClient.Builder().build()
                return Observable.fromCallable {
                    httpClient.newCall(Request.Builder().url(uri.toString()).build()).execute()
                }.subscribeOn(Schedulers.io())
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val ksApplication = requireNotNull(extras[APPLICATION_KEY] as? KSApplication) {
                "KSApplication must be provided in CreationExtras"
            }
            return DeepLinkViewModel(
                environment = environment,
                application = ksApplication,
                intent = intent,
                externalCall = customNetworkClient ?: externalCall
            ) as T
        }
    }
}
