package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Test

class ProxyOnlyAppsTargetResolverTest {
    @Test
    fun `returns selected packages in normal mode`() {
        val targetPackages = ProxyOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b", "c"),
            selectedPackages = setOf("a", "c"),
            invert = false,
            excludedPackages = setOf("c")
        )

        assertEquals(setOf("a"), targetPackages)
    }

    @Test
    fun `returns complement packages in invert mode`() {
        val targetPackages = ProxyOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b", "c"),
            selectedPackages = setOf("a"),
            invert = true,
            excludedPackages = setOf("c")
        )

        assertEquals(setOf("b"), targetPackages)
    }

    @Test
    fun `ignores unknown selected packages`() {
        val targetPackages = ProxyOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b"),
            selectedPackages = setOf("a", "missing"),
            invert = false
        )

        assertEquals(setOf("a"), targetPackages)
    }
}
