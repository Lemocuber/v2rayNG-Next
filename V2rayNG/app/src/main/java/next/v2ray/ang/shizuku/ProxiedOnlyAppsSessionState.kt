package next.v2ray.ang.shizuku

data class ProxiedOnlyAppsSessionState(
    val active: Boolean,
    val appliedPackages: Set<String>,
    val appliedState: ProxiedOnlyAppsPackageState?
)

object ProxiedOnlyAppsSessionReducer {
    fun onStart(
        requestedPackages: Set<String>,
        appliedState: ProxiedOnlyAppsPackageState,
        failedPackages: Set<String>
    ): ProxiedOnlyAppsSessionState {
        val appliedPackages = requestedPackages - failedPackages
        return ProxiedOnlyAppsSessionState(
            active = appliedPackages.isNotEmpty(),
            appliedPackages = appliedPackages,
            appliedState = appliedState.takeIf { appliedPackages.isNotEmpty() }
        )
    }

    fun onStop(previousState: ProxiedOnlyAppsSessionState, failedPackages: Set<String>): ProxiedOnlyAppsSessionState {
        val remainingPackages = previousState.appliedPackages.intersect(failedPackages)
        return ProxiedOnlyAppsSessionState(
            active = remainingPackages.isNotEmpty(),
            appliedPackages = remainingPackages,
            appliedState = previousState.appliedState.takeIf { remainingPackages.isNotEmpty() }
        )
    }
}
