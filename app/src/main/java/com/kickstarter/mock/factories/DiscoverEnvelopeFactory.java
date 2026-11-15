package com.kickstarter.mock.factories;

import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Test mock implementation for DiscoverEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
public final class DiscoverEnvelopeFactory {
  private DiscoverEnvelopeFactory() {}

  public static @NonNull DiscoverEnvelope discoverEnvelope(final @NonNull List<Project> projects) {
    return DiscoverEnvelope.builder()
      .projects(projects)
      .urls(
        DiscoverEnvelope.UrlsEnvelope.builder()
          .api(DiscoverEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("").build())
          .build()
      )
      .stats(
        DiscoverEnvelope
          .StatsEnvelope
          .builder()
          .count(10)
          .build()
      )
      .build();
  }
}
