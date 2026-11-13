package com.kickstarter.features.projectpage.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.data.LoginReason
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

class ProjectNavigationDelegateTest : KSRobolectricTestCase() {

    private lateinit var activity: Activity
    private lateinit var delegate: ProjectNavigationDelegate

    @Before
    override fun setUp() {
        super.setUp()
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        delegate = ProjectNavigationDelegate(
            activity = activity,
            environment = environment(),
            ksString = requireNotNull(environment().ksString())
        )
    }

    @Test
    fun testStartShareIntent() {
        delegate.startShareIntent(Pair("Great Project", "https://kickstarter.com/projects/123/great"))

        val nextIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(nextIntent)
        assertEquals(Intent.ACTION_CHOOSER, nextIntent.action)
        val targetIntent = nextIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(targetIntent)
        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("text/plain", targetIntent?.type)
        val text = targetIntent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        assertTrue(text.contains("Great Project"))
        assertTrue(text.contains("https://kickstarter.com/projects/123/great"))
    }

    @Test
    fun testStartLoginToutActivity() {
        delegate.startLoginToutActivity()

        val shadow = shadowOf(activity)
        val startedIntent = shadow.nextStartedActivityForResult
        assertNotNull(startedIntent)
        assertEquals(ActivityRequestCodes.LOGIN_FLOW, startedIntent.requestCode)
        assertEquals(LoginToutActivity::class.java.name, startedIntent.intent.component?.className)
        assertEquals(LoginReason.STAR_PROJECT, startedIntent.intent.getSerializableExtra(IntentKey.LOGIN_REASON))
    }

    @Test
    fun testStartMessagesActivity() {
        val project = ProjectFactory.project()
        delegate.startMessagesActivity(project)

        val nextIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(nextIntent)
        assertEquals(MessagesActivity::class.java.name, nextIntent.component?.className)
        assertNotNull(nextIntent.getParcelableExtra(IntentKey.PROJECT))
    }

    @Test
    fun testOpenProjectAndFinish() {
        delegate.openProjectAndFinish("https://kickstarter.com/projects/123")
        assertTrue(activity.isFinishing)
    }
}
