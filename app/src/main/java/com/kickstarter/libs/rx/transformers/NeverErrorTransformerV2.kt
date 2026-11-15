package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Consumer

/**
 * Library utility component for NeverErrorTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
open class NeverErrorTransformerV2<T> : ObservableTransformer<T, T> {
    private val errorAction: Consumer<Throwable>?

    constructor() {
        errorAction = null
    }

    protected constructor(errorAction: Consumer<Throwable>?) {
        this.errorAction = errorAction
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .doOnError { e: Throwable ->
                errorAction?.accept(e)
            }
            .onErrorResumeNext(Observable.empty())
    }
}
