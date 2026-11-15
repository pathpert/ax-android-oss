package com.kickstarter.libs.qualifiers

import javax.inject.Qualifier

/**
 * Dagger dependency injection qualifier annotation for ApiRetrofit.
 *
 * Negative constraints:
 * - Do not use outside Dagger dependency injection modules or bindings.
 */
@Qualifier
annotation class ApiRetrofit
@Qualifier
annotation class ApiRetrofitV2
