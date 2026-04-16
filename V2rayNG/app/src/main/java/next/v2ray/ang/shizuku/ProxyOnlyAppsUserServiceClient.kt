package next.v2ray.ang.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import next.v2ray.ang.AppConfig
import next.v2ray.ang.BuildConfig
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ProxyOnlyAppsUserServiceClient : ProxyOnlyAppsServiceGateway {
    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, ProxyOnlyAppsUserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("proxy_only_apps")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    override fun setPackagesState(packageNames: Collection<String>, state: ProxyOnlyAppsPackageState): Set<String> {
        val packages = packageNames.distinct()
        if (packages.isEmpty()) return emptySet()
        if (!ShizukuRuntime.isReady() || Shizuku.getVersion() < 10) return packages.toSet()

        var service: IProxyOnlyAppsService? = null
        val latch = CountDownLatch(1)
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = binder?.let(IProxyOnlyAppsService.Stub::asInterface)
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        return try {
            Shizuku.bindUserService(userServiceArgs, connection)
            if (!latch.await(5, TimeUnit.SECONDS)) {
                packages.toSet()
            } else {
                service?.setPackagesState(ArrayList(packages), state.packageManagerState)?.toSet() ?: packages.toSet()
            }
        } catch (e: Throwable) {
            Log.w(AppConfig.TAG, "ProxyOnlyApps: failed to bind user service", e)
            packages.toSet()
        } finally {
            runCatching { Shizuku.unbindUserService(userServiceArgs, connection, true) }
                .onFailure { Log.w(AppConfig.TAG, "ProxyOnlyApps: failed to unbind user service", it) }
        }
    }
}
