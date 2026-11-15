package com.kickstarter.libs.utils.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Utility and helper extensions for Disposable.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun Disposable.addToDisposable(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}
