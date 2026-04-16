# Android Package Name

The Android app's canonical package name is `next.v2ray.ang`.

## Build Configuration

- `V2rayNG/app/build.gradle.kts` sets both the Gradle `namespace` and the default `applicationId` to `next.v2ray.ang`.
- The F-Droid flavor appends `.fdroid`, so its final package name is `next.v2ray.ang.fdroid`.

## Source Layout

- Kotlin and AIDL sources live under `V2rayNG/app/src/main/java/next/v2ray/ang` and `V2rayNG/app/src/main/aidl/next/v2ray/ang`.
- Tests mirror the package under `V2rayNG/app/src/test/java/next/v2ray/ang`.

## Native Build Assumption

- `compile-hevtun.sh` passes `PKGNAME=next/v2ray/ang/service` when building the native tunnel helper, so any future package rename must update that flag alongside the Android sources.
