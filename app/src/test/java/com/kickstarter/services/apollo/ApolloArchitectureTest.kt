package com.kickstarter.services.apollo

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Architecture regression test enforcing that Apollo clients never emit bare
 * [Exception] or [java.lang.Exception] into reactive streams.
 *
 * Negative constraints:
 * - Do not allow raw throwable emissions in `ps.onError(...)` calls.
 */
class ApolloArchitectureTest {

    private val apolloClientDir = File("src/main/java/com/kickstarter/services/apollo").let {
        if (it.exists()) it else File("app/src/main/java/com/kickstarter/services/apollo")
    }

    @Test
    fun `verify no bare Exception or java lang Exception emissions in apollo clients`() {
        assertTrue("Apollo client directory must exist at ${apolloClientDir.absolutePath}", apolloClientDir.isDirectory)

        val violations = mutableListOf<String>()
        val bareExceptionPattern = Regex("""ps\.onError\(\s*(java\.lang\.)?Exception\(""")
        val rawThrowableOnErrorPattern = Regex("""\.doOnError\s*\{\s*throwable\s*->\s*ps\.onError\(throwable\)\s*\}""")

        apolloClientDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    if (bareExceptionPattern.containsMatchIn(line)) {
                        violations.add("${file.name}:${index + 1} emits bare Exception: ${line.trim()}")
                    }
                    if (rawThrowableOnErrorPattern.containsMatchIn(line)) {
                        violations.add("${file.name}:${index + 1} passes raw throwable: ${line.trim()}")
                    }
                }
            }

        assertTrue(
            "Found raw Exception violations in Apollo clients:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    @Test
    fun `verify all ps onError calls in apollo clients emit ProblemException`() {
        val nonProblemEmissions = mutableListOf<String>()
        val psOnErrorPattern = Regex("""ps\.onError\((.+)\)""")

        apolloClientDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    psOnErrorPattern.find(line)?.let { match ->
                        val arg = match.groupValues[1].trim()
                        val isAllowed = arg.startsWith("ProblemException(") ||
                            arg.startsWith("invalidProjectException(")
                        if (!isAllowed) {
                            nonProblemEmissions.add("${file.name}:${index + 1} -> ${line.trim()}")
                        }
                    }
                }
            }

        assertTrue(
            "Apollo client ps.onError calls not using ProblemException:\n${nonProblemEmissions.joinToString("\n")}",
            nonProblemEmissions.isEmpty()
        )
    }
}
