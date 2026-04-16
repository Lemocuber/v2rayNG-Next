package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Test

class ProxiedOnlyAppsOperationPlannerTest {
    @Test
    fun `normal mode enables selected apps on start`() {
        assertEquals(ProxiedOnlyAppsPackageState.ENABLED, ProxiedOnlyAppsOperationPlanner.stateOnStart(invert = false))
    }

    @Test
    fun `invert mode disables selected apps on start`() {
        assertEquals(ProxiedOnlyAppsPackageState.DISABLED, ProxiedOnlyAppsOperationPlanner.stateOnStart(invert = true))
    }

    @Test
    fun `stop uses reverse of applied start state`() {
        assertEquals(ProxiedOnlyAppsPackageState.DISABLED, ProxiedOnlyAppsPackageState.ENABLED.reverse())
        assertEquals(ProxiedOnlyAppsPackageState.ENABLED, ProxiedOnlyAppsPackageState.DISABLED.reverse())
    }
}
