package com.kickstarter.libs.recyclerviewpagination

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable

/**
 * Library utility component for RecyclerViewScrollEventOnSubscribe.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
internal class RecyclerViewScrollEventOnSubscribe(val recyclerView: RecyclerView) :
    ObservableOnSubscribe<RecyclerViewScrollEvent> {
    override fun subscribe(emitter: ObservableEmitter<RecyclerViewScrollEvent>) {
        MainThreadDisposable.verifyMainThread()
        val listener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!emitter.isDisposed) {
                    emitter.onNext(RecyclerViewScrollEvent.create(recyclerView, dx, dy))
                }
            }
        }
        recyclerView.addOnScrollListener(listener)
    }
}
