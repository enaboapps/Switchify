package com.enaboapps.switchify.service.window

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceHudMessageControllerTest {
    private val controller = ServiceHudMessageController(minDisplayMillis = 1000, collapseAfterMillis = 5000)

    private fun toast(text: String, duration: Long = 3000, key: String? = null) =
        ServiceHudMessage(text, durationMillis = duration, key = key)

    private fun status(text: String, key: String? = null) =
        ServiceHudMessage(text, durationMillis = null, key = key)

    @Test
    fun toastShowsImmediatelyAndHidesAfterItsDuration() {
        val frame = controller.show(toast("a"), now = 0)
        assertEquals("a", frame.message?.text)
        assertEquals(HudPresentation.TOAST, frame.presentation)
        assertEquals(3000L, frame.nextTickAt)

        assertEquals("a", controller.tick(2999).message?.text)
        assertEquals(HudFrame.HIDDEN, controller.tick(3000))
    }

    @Test
    fun rapidToastsQueueUntilMinimumDisplayThenAdvance() {
        controller.show(toast("a"), now = 0)
        val frame = controller.show(toast("b"), now = 200)
        assertEquals("a", frame.message?.text)
        assertEquals(1000L, frame.nextTickAt)

        assertEquals("a", controller.tick(999).message?.text)
        val advanced = controller.tick(1000)
        assertEquals("b", advanced.message?.text)
        assertEquals(4000L, advanced.nextTickAt)
    }

    @Test
    fun toastArrivingAfterMinimumDisplayReplacesTheCurrentOne() {
        controller.show(toast("a"), now = 0)
        val frame = controller.show(toast("b"), now = 1500)
        assertEquals("b", frame.message?.text)
        assertEquals(4500L, frame.nextTickAt)
    }

    @Test
    fun sameKeyReplacesOnScreenAndInQueue() {
        controller.show(toast("step 1", key = "pattern"), now = 0)
        val replaced = controller.show(toast("step 2", key = "pattern"), now = 100)
        assertEquals("step 2", replaced.message?.text)

        controller.show(toast("other"), now = 200)
        controller.show(toast("queued 1", key = "q"), now = 300)
        controller.show(toast("queued 2", key = "q"), now = 400)
        controller.tick(1100)
        assertEquals("other", controller.tick(1100).message?.text)
        assertEquals("queued 2", controller.tick(2100).message?.text)
        assertEquals(HudFrame.HIDDEN, controller.tick(6000))
    }

    @Test
    fun statusShowsAsBannerThenCollapsesToChip() {
        val frame = controller.show(status("paused"), now = 0)
        assertEquals(HudPresentation.BANNER, frame.presentation)
        assertEquals(5000L, frame.nextTickAt)

        assertEquals(HudPresentation.BANNER, controller.tick(4999).presentation)
        val collapsed = controller.tick(5000)
        assertEquals(HudPresentation.CHIP, collapsed.presentation)
        assertNull(collapsed.nextTickAt)
        assertEquals("paused", collapsed.message?.text)
    }

    @Test
    fun toastCoversStatusAndStatusReturnsAfterward() {
        controller.show(status("paused"), now = 0)
        val covered = controller.show(toast("saved"), now = 1000)
        assertEquals("saved", covered.message?.text)
        assertEquals(HudPresentation.TOAST, covered.presentation)

        val restored = controller.tick(4000)
        assertEquals("paused", restored.message?.text)
        assertEquals(HudPresentation.BANNER, restored.presentation)
        assertEquals(9000L, restored.nextTickAt)
    }

    @Test
    fun collapsedStatusReturnsAsChipAfterToast() {
        controller.show(status("paused"), now = 0)
        controller.tick(5000)
        controller.show(toast("saved"), now = 6000)
        val restored = controller.tick(9000)
        assertEquals(HudPresentation.CHIP, restored.presentation)
    }

    @Test
    fun statusReplacedWhileToastShowingAppearsAfterToast() {
        controller.show(status("first"), now = 0)
        controller.show(toast("saved"), now = 100)
        controller.show(status("second"), now = 200)
        val restored = controller.tick(3100)
        assertEquals("second", restored.message?.text)
        assertEquals(HudPresentation.BANNER, restored.presentation)
    }

    @Test
    fun statusShownWithNothingElseDoesNotStartCollapseUntilVisible() {
        controller.show(toast("busy"), now = 0)
        controller.show(status("paused"), now = 100)
        val restored = controller.tick(3000)
        assertEquals(HudPresentation.BANNER, restored.presentation)
        assertEquals(8000L, restored.nextTickAt)
    }

    @Test
    fun dismissAdvancesToastsAndDropsStatus() {
        controller.show(status("paused"), now = 0)
        controller.show(toast("a"), now = 100)
        controller.show(toast("b"), now = 200)
        val afterFirst = controller.dismiss(300)
        assertEquals("b", afterFirst.message?.text)
        val afterSecond = controller.dismiss(400)
        assertEquals("paused", afterSecond.message?.text)
        assertEquals(HudFrame.HIDDEN, controller.dismiss(500))
    }

    @Test
    fun dismissStatusByKeyLeavesOtherStatusesAndToastsAlone() {
        controller.show(status("trial ending", key = "trial"), now = 0)
        controller.show(toast("saved"), now = 100)
        assertEquals("saved", controller.dismissStatus(200, key = "pause").message?.text)
        assertEquals(true, controller.hasStatus())

        controller.dismissStatus(300, key = "trial")
        assertEquals(false, controller.hasStatus())
        assertEquals("saved", controller.tick(300).message?.text)
        assertEquals(HudFrame.HIDDEN, controller.tick(3100))
    }

    @Test
    fun dismissStatusWithoutKeyDropsAnyStatus() {
        controller.show(status("paused", key = "pause"), now = 0)
        assertEquals(HudFrame.HIDDEN, controller.dismissStatus(100))
        assertEquals(false, controller.hasStatus())
    }

    @Test
    fun statusReshowWithSameKeyReplacesInPlaceAndRestartsCollapse() {
        controller.show(status("paused", key = "pause"), now = 0)
        controller.tick(4000)
        val refreshed = controller.show(status("paused", key = "pause"), now = 4000)
        assertEquals(HudPresentation.BANNER, refreshed.presentation)
        assertEquals(9000L, refreshed.nextTickAt)
    }

    @Test
    fun clearHidesEverythingIncludingQueue() {
        controller.show(status("paused"), now = 0)
        controller.show(toast("a"), now = 100)
        controller.show(toast("b"), now = 200)
        assertEquals(HudFrame.HIDDEN, controller.clear(300))
        assertEquals(HudFrame.HIDDEN, controller.tick(10000))
    }
}
