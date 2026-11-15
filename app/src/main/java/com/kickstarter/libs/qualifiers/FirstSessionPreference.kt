package com.kickstarter.libs.qualifiers

import javax.inject.Qualifier

/**
 * Dagger dependency injection qualifier annotation for FirstSessionPreference.
 *
 * Negative constraints:
 * - Do not use outside Dagger dependency injection modules or bindings.
 */
@Qualifier
annotation class FirstSessionPreference
