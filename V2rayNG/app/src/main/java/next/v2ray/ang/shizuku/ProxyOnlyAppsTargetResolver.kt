package next.v2ray.ang.shizuku

object ProxyOnlyAppsTargetResolver {
    fun resolve(
        installedPackages: Set<String>,
        selectedPackages: Set<String>,
        invert: Boolean,
        excludedPackages: Set<String> = emptySet()
    ): Set<String> {
        val availablePackages = installedPackages - excludedPackages
        val allowedSelectedPackages = selectedPackages.intersect(availablePackages)
        return if (invert) availablePackages - allowedSelectedPackages else allowedSelectedPackages
    }
}
