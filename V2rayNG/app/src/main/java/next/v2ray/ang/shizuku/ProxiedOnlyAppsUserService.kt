package next.v2ray.ang.shizuku

import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import next.v2ray.ang.AppConfig

class ProxiedOnlyAppsUserService : IProxiedOnlyAppsService.Stub() {
    private val packageManager by lazy { resolvePackageManager() }
    private val setApplicationEnabledSettingMethod by lazy { resolveSetApplicationEnabledSettingMethod() }

    override fun destroy() {
        System.exit(0)
    }

    override fun setPackagesState(packageNames: MutableList<String>?, newState: Int): MutableList<String> {
        val packages = packageNames?.distinct().orEmpty()
        if (packages.isEmpty()) return mutableListOf()
        require(
            newState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        ) { "Unsupported package state: $newState" }

        val failedPackages = mutableListOf<String>()
        packages.forEach { packageName ->
            runCatching {
                invokeSetApplicationEnabledSetting(packageName, newState)
            }.onFailure {
                Log.w(AppConfig.TAG, "ProxiedOnlyApps: failed to update $packageName", it)
                failedPackages.add(packageName)
            }
        }
        return failedPackages
    }

    private fun invokeSetApplicationEnabledSetting(packageName: String, newState: Int) {
        val method = setApplicationEnabledSettingMethod
        val userId = resolveUserId()
        when (method.parameterTypes.size) {
            4 -> method.invoke(packageManager, packageName, newState, 0, userId)
            5 -> method.invoke(packageManager, packageName, newState, 0, userId, null)
            else -> error("Unsupported setApplicationEnabledSetting signature")
        }
    }

    private fun resolvePackageManager(): Any {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val getService = serviceManagerClass.getDeclaredMethod("getService", String::class.java)
        val packageBinder = getService.invoke(null, "package") as IBinder
        val stubClass = Class.forName("android.content.pm.IPackageManager\$Stub")
        val asInterface = stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
        return asInterface.invoke(null, packageBinder)
    }

    private fun resolveSetApplicationEnabledSettingMethod() = packageManager.javaClass.methods.firstOrNull {
        it.name == "setApplicationEnabledSetting" && it.parameterTypes.size in setOf(4, 5)
    } ?: error("setApplicationEnabledSetting not found")

    private fun resolveUserId(): Int = runCatching {
        val method = Class.forName("android.os.UserHandle").getDeclaredMethod("myUserId")
        method.invoke(null) as Int
    }.getOrDefault(0)
}
