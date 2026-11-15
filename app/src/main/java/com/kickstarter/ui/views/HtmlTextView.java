package com.kickstarter.ui.views;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * UI component rendering HtmlTextView.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
public final class HtmlTextView extends AppCompatTextView {
  public HtmlTextView(final @NonNull Context context) {
    super(context);
    init();
  }

  public HtmlTextView(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init(){
    setText(Html.fromHtml(getText().toString()));
  }
}
