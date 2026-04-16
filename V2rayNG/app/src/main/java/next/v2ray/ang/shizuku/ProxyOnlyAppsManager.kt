package next.v2ray.ang.shizuku

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import next.v2ray.ang.AngApplication
import next.v2ray.ang.AppConfig
import next.v2ray.ang.BuildConfig
import next.v2ray.ang.R
import next.v2ray.ang.extension.toastError
import next.v2ray.ang.handler.MmkvManager
import next.v2ray.ang.handler.SettingsManager
import next.v2ray.ang.service.V2RayVpnService
import next.v2ray.ang.util.AppManagerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ProxyOnlyAppsManager {
    private val excludedPackages = setOf(
        BuildConfig.APPLICATION_ID,
        "moe.shizuku.privileged.api",
        "rikka.sui"
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val operationMutex = Mutex()

    internal var serviceGateway: ProxyOnlyAppsServiceGateway = ProxyOnlyAppsUserServiceClient

    fun handleVpnStart(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            operationMutex.withLock {
                handleVpnStartLocked(appContext)
            }
        }
    }

    fun handleVpnStop() {
        scope.launch {
            operationMutex.withLock {
                handleVpnStopLocked()
            }
        }
    }

    fun reconcileIfNeeded(context: Context) {
        if (!ShizukuRuntime.isReady()) return
        val appContext = context.applicationContext
        scope.launch {
            operationMutex.withLock {
                if (!loadSession().active) return@withLock
                if (isVpnServiceRunning(appContext)) return@withLock
                handleVpnStopLocked()
            }
        }
    }

    private fun handleVpnStartLocked(context: Context) {
        recoverPreviousSessionIfNeededLocked()
        if (loadSession().active) {
            Log.w(AppConfig.TAG, "ProxyOnlyApps: previous session still active, skipping start")
            return
        }

        if (!SettingsManager.isVpnMode() || !MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS, false)) {
            clearSession()
            return
        }
        if (!ShizukuRuntime.isReady()) {
            clearSession()
            return
        }
        if (!isVpnServiceRunning(context)) {
            clearSession()
            return
        }

        val targetPackages = resolveTargetPackages(context)
        if (targetPackages.isEmpty()) {
            clearSession()
            return
        }

        val appliedState = ProxyOnlyAppsOperationPlanner.stateOnStart(
            MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_INVERT, false)
        )
        val failedPackages = serviceGateway.setPackagesState(targetPackages, appliedState)
        persistSession(
            ProxyOnlyAppsSessionReducer.onStart(
                requestedPackages = targetPackages,
                appliedState = appliedState,
                failedPackages = failedPackages
            )
        )
        if (failedPackages.isNotEmpty()) showApplyFailureToast()
    }

    private fun handleVpnStopLocked() {
        val session = loadSession()
        if (!session.active) return
        if (!ShizukuRuntime.isReady()) return

        val failedPackages = serviceGateway.setPackagesState(
            session.appliedPackages,
            session.appliedState?.reverse() ?: inferAppliedStateFromSettings().reverse()
        )
        persistSession(
            ProxyOnlyAppsSessionReducer.onStop(
                previousState = session,
                failedPackages = failedPackages
            )
        )
        if (failedPackages.isNotEmpty()) showApplyFailureToast()
    }

    private fun recoverPreviousSessionIfNeededLocked() {
        if (loadSession().active) handleVpnStopLocked()
    }

    private fun resolveTargetPackages(context: Context): Set<String> = ProxyOnlyAppsTargetResolver.resolve(
        installedPackages = AppManagerUtil.loadInstalledPackageNames(context),
        selectedPackages = MmkvManager.decodeSettingsStringSet(AppConfig.PREF_PROXY_ONLY_APPS_SET)?.toSet().orEmpty(),
        excludedPackages = excludedPackages
    )

    private fun loadSession(): ProxyOnlyAppsSessionState {
        val appliedPackages = MmkvManager.decodeSettingsStringSet(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET)?.toSet().orEmpty()
        val active = MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false) && appliedPackages.isNotEmpty()
        val appliedState = ProxyOnlyAppsPackageState.fromStorageValue(
            MmkvManager.decodeSettingsString(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_STATE)
        ) ?: inferAppliedStateFromSettings().takeIf { active }

        return ProxyOnlyAppsSessionState(
            active = active && appliedState != null,
            appliedPackages = appliedPackages,
            appliedState = appliedState
        )
    }

    private fun inferAppliedStateFromSettings() = ProxyOnlyAppsOperationPlanner.stateOnStart(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_PROXY_ONLY_APPS_INVERT, false)
    )

    private fun persistSession(session: ProxyOnlyAppsSessionState) {
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, session.active)
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET, session.appliedPackages.toMutableSet())
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_STATE, session.appliedState?.storageValue.orEmpty())
    }

    private fun clearSession() {
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_SESSION_ACTIVE, false)
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_SET, mutableSetOf<String>())
        MmkvManager.encodeSettings(AppConfig.PREF_PROXY_ONLY_APPS_LAST_APPLIED_STATE, "")
    }

    private fun showApplyFailureToast() {
        scope.launch(Dispatchers.Main) {
            AngApplication.application.toastError(R.string.toast_poa_apply_failed)
        }
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
