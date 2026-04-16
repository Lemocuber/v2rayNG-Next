package next.v2ray.ang.shizuku

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

object ShizukuRuntime {
    fun init(context: Context?) {
        if (context == null) return
        ShizukuProvider.enableMultiProcessSupport(context.packageName == currentProcessName())
    }

    fun onAppCreate(context: Context) {
        if (context.packageName != currentProcessName()) {
            ShizukuProvider.requestBinderForNonProviderProcess(context)
        }
    }

    fun isAvailable(): Boolean = runCatching {
        !Shizuku.isPreV11() && Shizuku.pingBinder()
    }.getOrDefault(false)

    fun hasPermission(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun isReady(): Boolean = isAvailable() && hasPermission()

    private fun currentProcessName() = next.v2ray.ang.util.ProcessUtil.currentProcessName()
}
