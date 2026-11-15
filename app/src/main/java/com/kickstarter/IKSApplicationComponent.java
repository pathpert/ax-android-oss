package com.kickstarter;

/**
 * Application component for IKSApplicationComponent.
 *
 * Negative constraints:
 * - Do not mutate internal state directly from external callers.
 */
public interface IKSApplicationComponent {
  ApplicationGraph getComponent();
}
