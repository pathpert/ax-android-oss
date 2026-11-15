package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.ClearUserUnseenActivityMutation
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.UserPrivacyQuery
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.transformers.userPrivacyTransformer
import com.kickstarter.type.CurrencyCode
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for user privacy, credentials, verification, and preferences.
 */
class ApolloUserClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    val idempotencyGuard: IdempotencyGuard = IdempotencyGuard()
) {
    fun userPrivacy(): Observable<UserPrivacy> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacy>()
            val query = UserPrivacyQuery()
            service.query(query = query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    response.data?.me?.let {
                        ps.onNext(userPrivacyTransformer(it))
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun updateUserPassword(
        currentPassword: String = "",
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserPasswordMutation.Data>()
            val mutation = UpdateUserPasswordMutation(
                currentPassword = currentPassword,
                password = newPassword,
                passwordConfirmation = confirmPassword
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            val mutation = UpdateUserEmailMutation(
                email = email,
                currentPassword = currentPassword
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        ps.onNext(data)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SendEmailVerificationMutation.Data>()
            val mutation = SendEmailVerificationMutation()
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        ps.onNext(data)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserCurrencyMutation.Data>()
            val mutation = UpdateUserCurrencyMutation(
                chosenCurrency = currency
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatePasswordMutation.Data>()
            val mutation = CreatePasswordMutation(
                password = password,
                passwordConfirmation = confirmPassword
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun clearUnseenActivity(): Observable<Int> {
        return Observable.defer {
            val ps = PublishSubject.create<Int>()
            val mutation = ClearUserUnseenActivityMutation()
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    response.data?.clearUserUnseenActivity?.activityIndicatorCount?.let {
                        ps.onNext(it)
                    } ?: ps.onError(Exception())
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
