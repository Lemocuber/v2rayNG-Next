package next.v2ray.ang.shizuku

data class ProxyOnlyAppsSessionState(
    val active: Boolean,
    val appliedPackages: Set<String>
)

object ProxyOnlyAppsSessionReducer {
    fun onStart(requestedPackages: Set<String>, failedPackages: Set<String>): ProxyOnlyAppsSessionState {
        val appliedPackages = requestedPackages - failedPackages
        return ProxyOnlyAppsSessionState(active = appliedPackages.isNotEmpty(), appliedPackages = appliedPackages)
    }

    fun onStop(previousPackages: Set<String>, failedPackages: Set<String>): ProxyOnlyAppsSessionState {
        val remainingPackages = previousPackages.intersect(failedPackages)
        return ProxyOnlyAppsSessionState(active = remainingPackages.isNotEmpty(), appliedPackages = remainingPackages)
    }
}
