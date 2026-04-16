package next.v2ray.ang.shizuku

import org.junit.Assert.assertEquals
import org.junit.Test

class ProxiedOnlyAppsTargetResolverTest {
    @Test
    fun `returns selected packages after exclusions`() {
        val targetPackages = ProxiedOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b", "c"),
            selectedPackages = setOf("a", "c"),
            excludedPackages = setOf("c")
        )

        assertEquals(setOf("a"), targetPackages)
    }

    @Test
    fun `does not invert the target set`() {
        val targetPackages = ProxiedOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b", "c"),
            selectedPackages = setOf("a"),
            excludedPackages = setOf("c")
        )

        assertEquals(setOf("a"), targetPackages)
    }

    @Test
    fun `ignores unknown selected packages`() {
        val targetPackages = ProxiedOnlyAppsTargetResolver.resolve(
            installedPackages = setOf("a", "b"),
            selectedPackages = setOf("a", "missing")
        )

        assertEquals(setOf("a"), targetPackages)
    }
}
