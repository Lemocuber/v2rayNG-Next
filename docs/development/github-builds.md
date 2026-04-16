# GitHub Build Variants

This repository uses separate GitHub Actions build paths for the release branch and the development branch.

## Branch Behavior

- `master` uses [`.github/workflows/build.yml`](../../.github/workflows/build.yml) to produce signed release APKs for the default distribution flow.
- `dev` uses [`.github/workflows/build-dev.yml`](../../.github/workflows/build-dev.yml) to produce a single unsigned `playstoreRelease` APK for `arm64-v8a` on every push.

## Dev Build Constraints

- The `dev` workflow does not decode or require app-signing secrets.
- The `dev` workflow does not publish GitHub release assets.
- The `dev` workflow limits `ABI_FILTERS` to `arm64-v8a`, so CI produces one APK instead of the full ABI matrix.
- The `dev` workflow keeps the default `playstore` flavor and only changes the signing and artifact scope.

## Android Build Notes

- The app module uses an AIDL Binder contract for Proxy Only Apps at `V2rayNG/app/src/main/aidl/com/v2ray/ang/shizuku/IProxyOnlyAppsService.aidl`.
- On AGP 9.x, `V2rayNG/app/build.gradle.kts` must keep `buildFeatures.aidl = true` so the generated `IProxyOnlyAppsService` classes exist during `compilePlaystoreReleaseKotlin`.
