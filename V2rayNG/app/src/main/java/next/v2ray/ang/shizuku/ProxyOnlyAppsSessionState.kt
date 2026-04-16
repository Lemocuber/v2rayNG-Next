package next.v2ray.ang.shizuku

data class ProxyOnlyAppsSessionState(
    val active: Boolean,
    val appliedPackages: Set<String>,
    val appliedState: ProxyOnlyAppsPackageState?
)

object ProxyOnlyAppsSessionReducer {
    fun onStart(
        requestedPackages: Set<String>,
        appliedState: ProxyOnlyAppsPackageState,
        failedPackages: Set<String>
    ): ProxyOnlyAppsSessionState {
        val appliedPackages = requestedPackages - failedPackages
        return ProxyOnlyAppsSessionState(
            active = appliedPackages.isNotEmpty(),
            appliedPackages = appliedPackages,
            appliedState = appliedState.takeIf { appliedPackages.isNotEmpty() }
        )
    }

    fun onStop(previousState: ProxyOnlyAppsSessionState, failedPackages: Set<String>): ProxyOnlyAppsSessionState {
        val remainingPackages = previousState.appliedPackages.intersect(failedPackages)
        return ProxyOnlyAppsSessionState(
            active = remainingPackages.isNotEmpty(),
            appliedPackages = remainingPackages,
            appliedState = previousState.appliedState.takeIf { remainingPackages.isNotEmpty() }
        )
    }
}
