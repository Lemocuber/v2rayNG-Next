package next.v2ray.ang.shizuku

object ProxiedOnlyAppsTargetResolver {
    fun resolve(
        installedPackages: Set<String>,
        selectedPackages: Set<String>,
        excludedPackages: Set<String> = emptySet()
    ): Set<String> {
        val availablePackages = installedPackages - excludedPackages
        return selectedPackages.intersect(availablePackages)
    }
}
