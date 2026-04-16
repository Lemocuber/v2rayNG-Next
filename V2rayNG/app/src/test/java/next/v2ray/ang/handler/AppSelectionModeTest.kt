package next.v2ray.ang.handler

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSelectionModeTest {
    @Test
    fun `per app proxy keeps system apps in selection list`() {
        assertTrue(AppSelectionMode.PER_APP_PROXY.shouldIncludeInSelectionList(isSystemApp = true))
        assertTrue(AppSelectionMode.PER_APP_PROXY.shouldIncludeInSelectionList(isSystemApp = false))
    }

    @Test
    fun `proxied only apps hides system apps from selection list`() {
        assertFalse(AppSelectionMode.PROXIED_ONLY_APPS.shouldIncludeInSelectionList(isSystemApp = true))
        assertTrue(AppSelectionMode.PROXIED_ONLY_APPS.shouldIncludeInSelectionList(isSystemApp = false))
    }
}
