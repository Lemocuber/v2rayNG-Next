package com.v2ray.ang.shizuku

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayVpnService
import com.v2ray.ang.util.AppManagerUtil

object ProxyOnlyAppsManager {
    private val excludedPackages = setOf(
        BuildConfig.APPLICATION_ID,
        "moe.shizuku.privileged.api",
        "rikka.sui"
    )

    fun handleVpnStart(context: Context) {
        recoverPreviousSessionIfNeeded()
        if (!MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS, false)) {
            clearSession()
            return
        }

        val targetPackages = resolveTargetPackages(context)
        if (targetPackages.isEmpty()) {
            clearSession()
            return
        }

        persistSession(
            ProxyOnlyAppsSessionReducer.onStart(
                requestedPackages = targetPackages,
                failedPackages = ProxyOnlyAppsUserServiceClient.setPackagesEnabled(targetPackages, true)
            )
        )
    }

    fun handleVpnStop() {
        if (!MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false)) return
        val appliedPackages = MmkvManager.decodeSettingsStringSet(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET)?.toSet().orEmpty()
        if (appliedPackages.isEmpty()) {
            clearSession()
            return
        }

        persistSession(
            ProxyOnlyAppsSessionReducer.onStop(
                previousPackages = appliedPackages,
                failedPackages = ProxyOnlyAppsUserServiceClient.setPackagesEnabled(appliedPackages, false)
            )
        )
    }

    fun reconcileIfNeeded(context: Context) {
        if (!ShizukuRuntime.isReady()) return
        if (!MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false)) return
        if (isVpnServiceRunning(context)) return
        handleVpnStop()
    }

    private fun recoverPreviousSessionIfNeeded() {
        if (MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false)) {
            handleVpnStop()
        }
    }

    private fun resolveTargetPackages(context: Context): Set<String> = ProxyOnlyAppsTargetResolver.resolve(
        installedPackages = AppManagerUtil.loadInstalledPackageNames(context),
        selectedPackages = MmkvManager.decodeSettingsStringSet(AppConfig.PREF_PROXY_ONLY_APPS_SET)?.toSet().orEmpty(),
        invert = MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_INVERT, false),
        excludedPackages = excludedPackages
    )

    private fun persistSession(session: ProxyOnlyAppsSessionState) {
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, session.active)
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET, session.appliedPackages.toMutableSet())
    }

    private fun clearSession() {
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false)
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET, mutableSetOf<String>())
    }

    @Suppress("DEPRECATION")
    private fun isVpnServiceRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        return runCatching {
            activityManager.getRunningServices(Int.MAX_VALUE).any { serviceInfo ->
                serviceInfo.service.className == V2RayVpnService::class.java.name
            }
        }.onFailure {
            Log.w(AppConfig.TAG, "ProxyOnlyApps: failed to inspect running services", it)
        }.getOrDefault(false)
    }
}
