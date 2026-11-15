package com.kickstarter.libs.rx.transformers

import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * Library utility component for ErrorsTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class ErrorsTransformerV2<T> : ObservableTransformer<Notification<T>, Throwable?> {
    override fun apply(upstream: Observable<Notification<T>>): ObservableSource<Throwable?> {
        return upstream
            .filter { obj: Notification<T> -> obj.isOnError }
            .map { obj: Notification<T> -> obj.error }
    }
}
