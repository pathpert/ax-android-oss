package com.kickstarter.libs.rx.transformers

import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * Library utility component for ValuesTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class ValuesTransformerV2<T> : ObservableTransformer<Notification<T>, T?> {
    override fun apply(upstream: Observable<Notification<T>>): ObservableSource<T?> {
        return upstream
            .filter { obj: Notification<T> -> obj.isOnNext }
            .map { obj: Notification<T> -> obj.value }
    }
}
