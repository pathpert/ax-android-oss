# Rules: Codebase Modernization for Agent Experience (AX)

You are an expert AI software engineer refactoring the `ax-android-oss` (Android / Kotlin / Jetpack Compose) brownfield codebase to support Agent Experience (AX) and Agent-Native Architecture.
When transforming or extending existing code in this repository, strictly enforce the following architectural rules:

---

### Rule 1: Apply the Strangler Fig Pattern (Never Rewrite Wholesale)
- **Do not rewrite existing monolithic files in a single pass** (e.g., legacy classes under `app/src/main/java/com/kickstarter/viewmodels/`, `ui/`, or `services/`).
- Isolate refactored or newly created sub-domains into self-contained feature packages under `app/src/main/java/com/kickstarter/features/{feature}/`.
- When legacy Java/RxJava2 components (`ApiClientTypeV2`, legacy `models/`, `ApplicationModule`) must still be used, wrap them behind a strictly-typed Kotlin adapter or repository interface rather than letting legacy nullable Java types or raw API payloads leak across the feature boundary.

---

### Rule 2: Component-Local Modularization (Dev-Primitives)
Every refactored or newly created feature MUST be autonomous and colocate its artifacts under `com.kickstarter.features.{feature}`:
```text
app/src/main/java/com/kickstarter/features/{feature}/
├── data/        # Immutable Kotlin contracts, sealed UI/error states, & boundary mappers
├── viewmodel/   # ViewModels & unidirectional state reducers
├── ui/          # Jetpack Compose screens/components & thin Activities/Fragments
└── extensions/  # Feature-local Kotlin extensions (when needed)

app/src/test/java/com/kickstarter/features/{feature}/
└── ...          # Scoped JUnit4 / Robolectric unit tests mirroring the feature
```
- Keep individual source files concise (target < 300 lines) to prevent attention decay and context window bloat.

---

### Rule 3: Poka-Yoke & Contract-First Typing
- **Eliminate loose typing:** Strictly forbid `Any`, `Any?` (without explicit narrowing), untyped dictionaries (`Map<String, Any>`), raw `Bundle`/`Intent` string keys without typed mappers, and unsafe casts (`as`, `!!`) across feature boundaries.
- **Validate at the boundary:** Normalize and validate all external inputs (Apollo GraphQL generated models, Retrofit/Gson responses, deep links, Intent extras) into immutable Kotlin `data class`es, `@JvmInline value class`es, or `sealed interface` hierarchies.
- **Enrich contract KDoc:** Public domain models, schema fields, and action parameters MUST include concise KDoc specifying:
  1. Exact business meaning, units, and expected format (e.g., ISO 8601 timestamp, integer minor currency units, non-blank project ID).
  2. Explicit **Negative Constraints** stating what MUST NOT be passed (e.g., `"Do not pass floating-point currency; use integer minor units"` or `"Do not pass raw Apollo generated models into Compose UI"`).

---

### Rule 4: In-Band Actionable Errors (RFC 9457 Semantics)
- **Never swallow errors:** Forbid empty `catch` blocks, silent `onErrorReturn { null }`, or dropping Coroutines/RxJava2 errors without updating state.
- **Never expose raw exceptions or opaque generic errors** (e.g., raw stack traces or uncontextualized `"Something went wrong"`).
- Reuse the shared primitives in `com.kickstarter.ax.errors` (`ProblemDetails`, `DomainResult`, `ProblemDetailsMapper`) and `com.kickstarter.ax.types` (`MinorCurrencyAmount`, `NonBlankId`, `RateLimitBackoff`):
  ```kotlin
  data class ProblemDetails(
      val type: String, // e.g., "urn:kickstarter:error:validation"
      val title: String,
      val status: Int? = null,
      val detail: String,
      val suggestedAction: String, // Clear remediation instruction for caller/UI
      val isRetriable: Boolean,
      val retryAfterSeconds: Long? = null,
      val invalidParams: List<InvalidParam> = emptyList(),
  ) {
      data class InvalidParam(val name: String, val reason: String)
  }
  ```

---

### Rule 5: Machine-Tempo Safety & Idempotency
- Guard all mutating operations (Apollo GraphQL mutations, Retrofit `POST`/`PUT`/`DELETE`, pledge/checkout actions) against duplicate execution via `com.kickstarter.ax.safety.IdempotencyGuard` and `IdempotencyKey` (`Idempotency-Key` header or client mutation ID where supported).
- Respect server rate-limit and backoff signals (`Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining`, HTTP 429/503) via `RateLimitBackoff` instead of performing tight retry loops.

---

### Rule 6: Deprecate Legacy Paths Loudly
- When replacing obsolete Java/Kotlin classes, ViewModels, or `ApiClientTypeV2` methods, annotate them loudly:
  - Kotlin: `@Deprecated(message = "Replaced by ...", replaceWith = ReplaceWith("..."))`
  - Java: `@Deprecated` with Javadoc `@deprecated` pointing to the replacement class/package.
- Explicitly document the recommended replacement feature primitive so engineers and automated agents do not route new code to legacy paths.

---

### Rule 7: Carried Headless Verification
- Every refactored or newly created feature MUST include unit tests under `app/src/test/java/com/kickstarter/features/{feature}/` verifying:
  1. Happy path state transitions and boundary mapping.
  2. Edge cases and invalid input / boundary rejections.
  3. Actionable error mapping (`suggestedAction`, `isRetriable`, `invalidParams`, status codes).
- Keep tests locally runnable via scoped, deterministic Gradle commands without requiring full-repo instrumentation suites:
  - Scoped unit tests: `./gradlew :app:testExternalDebugUnitTest --tests "com.kickstarter.features.{feature}.*"`
  - Style & static checks: `./gradlew ktlint checkstyle`

