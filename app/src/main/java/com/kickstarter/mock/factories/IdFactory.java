package com.kickstarter.mock.factories;

/**
 * Test mock implementation for IdFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
public final class IdFactory {
  private IdFactory() {}

  static int id = 1;

  public static synchronized int id() {
    return id++;
  }
}
