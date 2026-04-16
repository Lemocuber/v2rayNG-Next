package next.v2ray.ang.shizuku;

interface IProxyOnlyAppsService {
    void destroy() = 16777114;

    List<String> setPackagesState(in List<String> packageNames, int newState) = 1;
}
