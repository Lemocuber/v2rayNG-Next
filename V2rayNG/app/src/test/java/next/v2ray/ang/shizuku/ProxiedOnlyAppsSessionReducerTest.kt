package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxiedOnlyAppsSessionReducerTest {
    @Test
    fun `start keeps only successfully applied packages`() {
        val state = ProxiedOnlyAppsSessionReducer.onStart(
            requestedPackages = setOf("a", "b"),
            appliedState = ProxiedOnlyAppsPackageState.ENABLED,
            failedPackages = setOf("b")
        )

        assertTrue(state.active)
        assertEquals(setOf("a"), state.appliedPackages)
        assertEquals(ProxiedOnlyAppsPackageState.ENABLED, state.appliedState)
    }

    @Test
    fun `start clears inactive session when all packages fail`() {
        val state = ProxiedOnlyAppsSessionReducer.onStart(
            requestedPackages = setOf("a"),
            appliedState = ProxiedOnlyAppsPackageState.DISABLED,
            failedPackages = setOf("a")
        )

        assertFalse(state.active)
        assertEquals(emptySet<String>(), state.appliedPackages)
        assertEquals(null, state.appliedState)
    }

    @Test
    fun `stop keeps retry set for failures`() {
        val state = ProxiedOnlyAppsSessionReducer.onStop(
            previousState = ProxiedOnlyAppsSessionState(
                active = true,
                appliedPackages = setOf("a", "b"),
                appliedState = ProxiedOnlyAppsPackageState.DISABLED
            ),
            failedPackages = setOf("b")
        )

        assertTrue(state.active)
        assertEquals(setOf("b"), state.appliedPackages)
        assertEquals(ProxiedOnlyAppsPackageState.DISABLED, state.appliedState)
    }

    @Test
    fun `stop clears session when everything is reverted`() {
        val state = ProxiedOnlyAppsSessionReducer.onStop(
            previousState = ProxiedOnlyAppsSessionState(
                active = true,
                appliedPackages = setOf("a"),
                appliedState = ProxiedOnlyAppsPackageState.ENABLED
            ),
            failedPackages = emptySet()
        )

        assertFalse(state.active)
        assertEquals(emptySet<String>(), state.appliedPackages)
        assertEquals(null, state.appliedState)
    }
}
