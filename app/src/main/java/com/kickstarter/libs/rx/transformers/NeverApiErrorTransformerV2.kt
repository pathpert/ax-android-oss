package com.kickstarter.libs.rx.transformers

import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.fromThrowable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Consumer
import timber.log.Timber

/**
 * Library utility component for NeverApiErrorTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
open class NeverApiErrorTransformerV2<T> : ObservableTransformer<T, T> {
    private val errorAction: Consumer<ErrorEnvelope>?

    constructor() {
        errorAction = null
    }

    protected constructor(errorAction: Consumer<ErrorEnvelope>?) {
        this.errorAction = errorAction
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .doOnError { e: Throwable ->
                val env = fromThrowable(e)
                if (env != null) {
                    errorAction?.accept(env)
                } else {
                    val problem = ProblemDetailsMapper.fromThrowable(e, "api stream")
                    Timber.w(ProblemException(problem, e), "Non-API error in NeverApiErrorTransformerV2: %s", problem.detail)
                }
            }
            .onErrorResumeNext { e: Throwable ->
                val env = fromThrowable(e)
                env?.let { Observable.empty() } ?: Observable.error(e)
            }
    }
}
