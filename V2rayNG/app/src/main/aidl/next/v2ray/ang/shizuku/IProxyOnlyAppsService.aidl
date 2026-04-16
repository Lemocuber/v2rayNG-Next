package next.v2ray.ang.shizuku;

interface IProxyOnlyAppsService {
    void destroy() = 16777114;

    List<String> setPackagesEnabled(in List<String> packageNames, boolean enabled) = 1;
}
