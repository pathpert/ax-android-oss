package com.kickstarter.libs.utils;

import android.os.Looper;

/**
 * Utility and helper extensions for ThreadUtils.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
public final class ThreadUtils {
  private ThreadUtils() {}

  public static boolean isMainThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }
}
