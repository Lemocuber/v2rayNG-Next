package next.v2ray.ang.shizuku

interface ProxiedOnlyAppsServiceGateway {
    fun setPackagesState(packageNames: Collection<String>, state: ProxiedOnlyAppsPackageState): Set<String>
}
