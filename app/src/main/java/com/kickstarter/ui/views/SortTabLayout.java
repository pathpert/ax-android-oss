package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;

/**
 * UI component rendering SortTabLayout.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
public final class SortTabLayout extends TabLayout {

  public SortTabLayout(final @NonNull Context context) {
    super(context);
  }

  public SortTabLayout(final @NonNull Context context, final @NonNull AttributeSet attrs) {
    super(context, attrs);
  }

  public SortTabLayout(final @NonNull Context context, final @NonNull AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
