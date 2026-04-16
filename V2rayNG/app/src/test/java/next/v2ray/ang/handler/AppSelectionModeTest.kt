package next.v2ray.ang.handler

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSelectionModeTest {
    @Test
    fun `per app proxy shows system apps by default`() {
        assertTrue(AppSelectionMode.PER_APP_PROXY.showsSystemAppsByDefault())
    }

    @Test
    fun `proxied only apps hides system apps by default`() {
        assertFalse(AppSelectionMode.PROXIED_ONLY_APPS.showsSystemAppsByDefault())
    }
}
