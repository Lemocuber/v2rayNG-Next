# Proxy Only Apps

Proxy Only Apps uses a Shizuku user service to toggle application enabled state for selected packages while the feature is active.

## Runtime Structure

- [IProxyOnlyAppsService.aidl](../../V2rayNG/app/src/main/aidl/next/v2ray/ang/shizuku/IProxyOnlyAppsService.aidl) defines the Binder contract shared by the app process and the Shizuku user service process.
- [ProxyOnlyAppsUserService.kt](../../V2rayNG/app/src/main/java/next/v2ray/ang/shizuku/ProxyOnlyAppsUserService.kt) runs in the Shizuku-managed process and reflects into `IPackageManager.setApplicationEnabledSetting(...)`.
- [ProxyOnlyAppsUserServiceClient.kt](../../V2rayNG/app/src/main/java/next/v2ray/ang/shizuku/ProxyOnlyAppsUserServiceClient.kt) binds the user service and sends the selected package list across the Binder boundary.

## Build Requirement

- The Android app module must keep `buildFeatures.aidl = true` in [V2rayNG/app/build.gradle.kts](../../V2rayNG/app/build.gradle.kts).
- This is required on AGP 9.x because the AIDL source under `app/src/main/aidl` is not generated unless the feature is explicitly enabled.
- If this flag is removed, Kotlin compilation fails with unresolved references to `IProxyOnlyAppsService` and its Binder methods.
