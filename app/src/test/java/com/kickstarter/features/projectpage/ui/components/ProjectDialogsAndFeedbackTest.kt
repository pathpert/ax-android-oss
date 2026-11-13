package com.kickstarter.features.projectpage.ui.components

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThanksActivity
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog

class ProjectDialogsAndFeedbackTest : KSRobolectricTestCase() {

    private lateinit var activity: FragmentActivity
    private lateinit var anchorView: View
    private lateinit var feedback: ProjectDialogsAndFeedback
    private var backStackCleared = false

    @Before
    override fun setUp() {
        super.setUp()
        activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        anchorView = View(activity)
        activity.setContentView(anchorView)
        backStackCleared = false
        feedback = ProjectDialogsAndFeedback(
            activity = activity,
            snackbarAnchor = anchorView,
            errorAnchor = anchorView,
            clearFragmentBackStack = {
                backStackCleared = true
                true
            }
        )
    }

    @Test
    fun testShowCancelPledgeSuccess_clearsBackStack() {
        feedback.showCancelPledgeSuccess()
        assertTrue(backStackCleared)
    }

    @Test
    fun testShowCreatePledgeSuccess_startsThanksActivity() {
        val project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build()
        val projectData = ProjectDataFactory.project(project)
        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.NEW_PLEDGE)
            .projectData(projectData)
            .reward(RewardFactory.reward())
            .build()
        val checkoutData = CheckoutDataFactory.checkoutData(0.0, 20.0)

        feedback.showCreatePledgeSuccess(checkoutData, pledgeData)

        assertTrue(backStackCleared)
        val nextIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(nextIntent)
        assertEquals(ThanksActivity::class.java.name, nextIntent.component?.className)
        assertNotNull(nextIntent.getParcelableExtra(IntentKey.CHECKOUT_DATA))
        assertNotNull(nextIntent.getParcelableExtra(IntentKey.PLEDGE_DATA))
    }

    @Test
    fun testShowPledgeNotCancelableDialog_showsAlertDialog() {
        feedback.showPledgeNotCancelableDialog()

        val dialog = ShadowAlertDialog.getLatestDialog()
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }
}
