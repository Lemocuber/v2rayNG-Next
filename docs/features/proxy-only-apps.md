# Proxied Only Apps

Proxied Only Apps is a VPN-mode-only feature that uses a Shizuku user service to toggle application enabled state for the selected packages while the VPN session is active.

## Behavior

- Normal mode: checked apps are enabled when VPN starts and disabled when VPN stops.
- Invert mode: checked apps are disabled when VPN starts and enabled when VPN stops.
- The checked package set is always the target set; invert mode changes the applied state, not the package selection.
- The selection UI exposes search plus clipboard import/export only; bulk actions available in per-app proxy (`Select all`, `Invert selection`, `Auto select proxy app`) are intentionally hidden here.
- VPN startup does not wait for Proxied Only Apps. The Shizuku apply step runs asynchronously after the VPN core starts.
- If any package operation fails, the app shows a single short toast: `Failed to apply POA`.

## Runtime Structure

- [IProxyOnlyAppsService.aidl](../../V2rayNG/app/src/main/aidl/next/v2ray/ang/shizuku/IProxyOnlyAppsService.aidl) defines the Binder contract shared by the app process and the Shizuku user service process.
- [ProxyOnlyAppsUserService.kt](../../V2rayNG/app/src/main/java/next/v2ray/ang/shizuku/ProxyOnlyAppsUserService.kt) runs in the Shizuku-managed process and reflects into `IPackageManager.setApplicationEnabledSetting(...)`.
- [ProxyOnlyAppsUserServiceClient.kt](../../V2rayNG/app/src/main/java/next/v2ray/ang/shizuku/ProxyOnlyAppsUserServiceClient.kt) binds the user service and applies an explicit package enabled state.
- [ProxyOnlyAppsManager.kt](../../V2rayNG/app/src/main/java/next/v2ray/ang/shizuku/ProxyOnlyAppsManager.kt) serializes start/stop reconciliation work on a background coroutine and persists the last successfully applied state for reversal.

## Build Requirement

- The Android app module must keep `buildFeatures.aidl = true` in [V2rayNG/app/build.gradle.kts](../../V2rayNG/app/build.gradle.kts).
- This is required on AGP 9.x because the AIDL source under `app/src/main/aidl` is not generated unless the feature is explicitly enabled.
- If this flag is removed, Kotlin compilation fails with unresolved references to `IProxyOnlyAppsService` and its Binder methods.
