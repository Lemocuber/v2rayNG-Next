package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Test

class ProxyOnlyAppsOperationPlannerTest {
    @Test
    fun `normal mode enables selected apps on start`() {
        assertEquals(ProxyOnlyAppsPackageState.ENABLED, ProxyOnlyAppsOperationPlanner.stateOnStart(invert = false))
    }

    @Test
    fun `invert mode disables selected apps on start`() {
        assertEquals(ProxyOnlyAppsPackageState.DISABLED, ProxyOnlyAppsOperationPlanner.stateOnStart(invert = true))
    }

    @Test
    fun `stop uses reverse of applied start state`() {
        assertEquals(ProxyOnlyAppsPackageState.DISABLED, ProxyOnlyAppsPackageState.ENABLED.reverse())
        assertEquals(ProxyOnlyAppsPackageState.ENABLED, ProxyOnlyAppsPackageState.DISABLED.reverse())
    }
}
