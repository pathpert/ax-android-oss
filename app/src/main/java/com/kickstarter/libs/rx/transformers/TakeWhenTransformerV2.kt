package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * Library utility component for TakeWhenTransformerV2.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class TakeWhenTransformerV2<S : Any, T>(private val `when`: Observable<T>) : ObservableTransformer<S, S> {
    override fun apply(upstream: Observable<S>): ObservableSource<S> {
        return `when`.withLatestFrom(upstream) { t: T, x: S -> x }
    }
}
