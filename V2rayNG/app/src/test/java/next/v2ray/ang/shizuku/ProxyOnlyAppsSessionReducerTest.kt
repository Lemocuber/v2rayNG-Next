package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyOnlyAppsSessionReducerTest {
    @Test
    fun `start keeps only successfully applied packages`() {
        val state = ProxyOnlyAppsSessionReducer.onStart(
            requestedPackages = setOf("a", "b"),
            failedPackages = setOf("b")
        )

        assertTrue(state.active)
        assertEquals(setOf("a"), state.appliedPackages)
    }

    @Test
    fun `start clears inactive session when all packages fail`() {
        val state = ProxyOnlyAppsSessionReducer.onStart(
            requestedPackages = setOf("a"),
            failedPackages = setOf("a")
        )

        assertFalse(state.active)
        assertEquals(emptySet<String>(), state.appliedPackages)
    }

    @Test
    fun `stop keeps retry set for failures`() {
        val state = ProxyOnlyAppsSessionReducer.onStop(
            previousPackages = setOf("a", "b"),
            failedPackages = setOf("b")
        )

        assertTrue(state.active)
        assertEquals(setOf("b"), state.appliedPackages)
    }

    @Test
    fun `stop clears session when everything is reverted`() {
        val state = ProxyOnlyAppsSessionReducer.onStop(
            previousPackages = setOf("a"),
            failedPackages = emptySet()
        )

        assertFalse(state.active)
        assertEquals(emptySet<String>(), state.appliedPackages)
    }
}
