package next.v2ray.ang.shizuku

interface ProxyOnlyAppsServiceGateway {
    fun setPackagesState(packageNames: Collection<String>, state: ProxyOnlyAppsPackageState): Set<String>
}
