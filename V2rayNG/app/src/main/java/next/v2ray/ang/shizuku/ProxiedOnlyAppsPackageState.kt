package next.v2ray.ang.shizuku

import android.content.pm.PackageManager

enum class ProxiedOnlyAppsPackageState(val storageValue: String, val packageManagerState: Int) {
    ENABLED("enabled", PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
    DISABLED("disabled", PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);

    fun reverse() = if (this == ENABLED) DISABLED else ENABLED

    companion object {
        fun fromStorageValue(value: String?) = entries.firstOrNull { it.storageValue == value }
    }
}

object ProxiedOnlyAppsOperationPlanner {
    fun stateOnStart(invert: Boolean) = if (invert) ProxiedOnlyAppsPackageState.DISABLED else ProxiedOnlyAppsPackageState.ENABLED
}
