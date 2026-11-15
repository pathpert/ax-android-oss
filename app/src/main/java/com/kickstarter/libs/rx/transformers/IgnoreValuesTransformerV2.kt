package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * Library utility component for IgnoreValuesTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class IgnoreValuesTransformerV2<S> : ObservableTransformer<S, Unit> {
    override fun apply(upstream: Observable<S>): ObservableSource<Unit> {
        return upstream.map { Unit }
    }
}
