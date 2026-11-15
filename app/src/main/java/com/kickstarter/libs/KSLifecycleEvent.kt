package com.kickstarter.libs

/**
 * Library utility component for KSLifecycleEvent.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
enum class KSLifecycleEvent {
    ATTACH, CREATE, CREATE_VIEW, START, RESUME, PAUSE, STOP, DESTROY_VIEW, DESTROY, DETACH
}
